package com.tanza.dashi;

import com.tanza.dashi.lib.Response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;

/**
 * @author jtanza
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Server {
    private static final int SELECTOR_TIME_OUT_MS = 1_000;

    private final int port;
    private final Executor workerPool;
    private final RequestDispatcher requestDispatcher;
    private final ChannelBuffer channelBuffer;

    public static Builder builder(RequestDispatcher requestDispatcher) {
        return new Builder(requestDispatcher);
    }

    public void serve() {
        ServerConnection serverConnection = ServerConnection.openConnection(port);
        Selector selector = serverConnection.getSelector();

        while (true) {
            try {
                selector.select(SELECTOR_TIME_OUT_MS);
            } catch (IOException e) {
                //TODO
                e.printStackTrace();
            }

            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
                handleSocketEvent(selector, key);
            }
        }
    }

    private void handleSocketEvent(Selector selector, SelectionKey key) {
        if (!key.isValid()) {
            return;
        }

        if (key.isAcceptable()) {
            registerChannelWithSelector(key, selector);
        } else if (key.isReadable()) {
            readThenWrite(key);
        }
    }

    private void readThenWrite(SelectionKey key) {
        channelBuffer.readFromChannel(key).ifPresent(read -> {
            Request request = Request.from(read);
            requestDispatcher.getHandlerFor(request).ifPresentOrElse(
                handler -> processRequestAsync(handler, key, request), respondNotFound(key)
            );
        });
    }

    private void processRequestAsync(RequestHandler handler, SelectionKey key, Request request) {
        CompletableFuture.supplyAsync(() -> handlerWrapper().apply(request, handler), workerPool)
            .thenAccept(response -> write(key, response))
            .thenAccept((c) -> Utils.closeConnection(key));
    }

    private static void write(SelectionKey key, Response response) {
        try {
            SocketChannel channel = (SocketChannel) key.channel();
            channel.write(response.toByteBuffer());
        } catch (IOException e) {
            e.printStackTrace();
            Utils.closeConnection(key);
        }
    }

    private static void registerChannelWithSelector(SelectionKey key, Selector selector) {
        try {
            SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
            // should always be a channel to accept, but guard
            if (socketChannel != null) {
                socketChannel.configureBlocking(false);
                socketChannel.register(selector, SelectionKey.OP_READ);
            }
        } catch (IOException e) {
            e.printStackTrace();
            key.cancel();
        }
    }

    private Runnable respondNotFound(SelectionKey key) {
        return () -> write(key, Response.notFound());
    }

    /**
     * Mutates the {@link Request} with path variables computed with data derived from a
     * {@link RequestHandler} before applying {@link RequestHandler#getAction()}
     *
     * @return
     */
    private static BiFunction<Request, RequestHandler, Response> handlerWrapper() {
        return (request, handler) -> {
            request.setPathVariables(handler);
            return handler.getAction().apply(request);
        };
    }

    public static class Builder {
        private static final int DEFAULT_PORT = 80;
        private static final int DEFAULT_MAX_READ = 1024 * 500;
        private static final Executor DEFAULT_THREAD_POOL = Executors.newCachedThreadPool();

        private int port = DEFAULT_PORT;
        private int maxFormSize = DEFAULT_MAX_READ;
        private Executor workerPool = DEFAULT_THREAD_POOL;

        private final RequestDispatcher requestDispatcher;

        public Builder(RequestDispatcher requestDispatcher) {
            this.requestDispatcher = requestDispatcher;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder maxFormSize(int maxFormSize) {
            this.maxFormSize = maxFormSize;
            return this;
        }

        public Builder workerPool(Executor workerPool) {
            this.workerPool = workerPool;
            return this;
        }

        public Server build() {
            return new Server(port, workerPool, requestDispatcher, new ChannelBuffer(maxFormSize));
        }

    }

    @Data
    private static class ServerConnection {
        private final Selector selector;
        private final ServerSocketChannel serverSocket;

        static ServerConnection openConnection(int port) {
            try {
                ServerSocketChannel serverSocket = ServerSocketChannel.open();
                serverSocket.configureBlocking(false);
                serverSocket.socket().bind(new InetSocketAddress(port));

                Selector selector = Selector.open();
                serverSocket.register(selector, SelectionKey.OP_ACCEPT);
                return new ServerConnection(selector, serverSocket);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
