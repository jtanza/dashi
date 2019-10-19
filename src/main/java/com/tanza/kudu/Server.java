package com.tanza.kudu;

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

/**
 * @author jtanza
 */
@AllArgsConstructor
public class Server {
    private static final int DEFAULT_PORT = 1024;
    private static final int SELECTOR_TIME_OUT_MS = 1_000;

    private final int port;
    private final RequestDispatcher requestDispatcher;
    private final Executor workerPool;

    public Server(RequestDispatcher requestDispatcher) {
        this.port = DEFAULT_PORT;
        this.requestDispatcher = requestDispatcher;
        this.workerPool = Executors.newCachedThreadPool();
    }

    public void serve() throws IOException {
        ServerConnection serverConnection = ServerConnection.openConnection(port);
        Selector selector = serverConnection.getSelector();

        while (true) {
            selector.select(SELECTOR_TIME_OUT_MS);

            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
                processSocketEvent(selector, key);
            }
        }
    }

    private void processSocketEvent(Selector selector, SelectionKey key) {
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
            requestDispatcher.getHandlerFor(request).ifPresent(handler -> processRequestAsync(handler, key, request));
        });
    }

    private void processRequestAsync(RequestHandler handler, SelectionKey key, Request request) {
        CompletableFuture.supplyAsync(() -> handler.getAction().apply(request), workerPool)
            .thenAccept(response -> write(key, response))
            .thenAccept((completable) -> Utils.closeConnection(key));
    }

    private static void write(SelectionKey key, Response response) {
        try {
            SocketChannel channel = (SocketChannel) key.channel();
            channel.write(response.toByteBuffer());
            Utils.closeConnection(key);
        } catch (IOException e) {
            e.printStackTrace();
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
