package com.tanza.dashi;

import com.tanza.dashi.lib.Response;

import lombok.Builder;
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
@Builder
public class Server {
    private static final int DEFAULT_PORT = 8080;
    private static final int SELECTOR_TIME_OUT_MS = 1_000;

    @Builder.Default
    private final int port = DEFAULT_PORT;
    @Builder.Default
    private final Executor workerPool = Executors.newCachedThreadPool();
    private final RequestDispatcher requestDispatcher;


    public static ServerBuilder builder(RequestDispatcher requestDispatcher) {
        return new ServerBuilder().requestDispatcher(requestDispatcher);
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
        ChannelBuffer.readFromChannel(key).ifPresent(read -> {
            Request request = Request.from(read);
            requestDispatcher.getHandlerFor(request).ifPresentOrElse(
                (handler) -> processRequestAsync(handler, key, request),
                () -> write(key, Response.notFound())
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

    /**
     * Mutates the {@link Request} with path variables computed with a
     * {@link RequestHandler} before applying {@link RequestHandler#getAction()}
     *
     * @return
     */
    private static BiFunction<Request, RequestHandler, Response> handlerWrapper() {
        return (request, handler) -> {
            request.parsePathVariables(handler);
            return handler.getAction().apply(request);
        };
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
