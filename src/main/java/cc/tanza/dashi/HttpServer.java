package cc.tanza.dashi;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;

import static cc.tanza.dashi.HttpConstants.StatusCode.NOT_FOUND;

/**
 * A {@link HttpServer} is responsible for connecting and responding to
 * HTTP request messages as they are received on a designated {@link #port}
 * over the network.
 *
 * Interfacing with client requests is achieved primarily through the use
 * of readiness selection offered via {@link Selector} and accompanying
 * classes in {@link java.nio}.
 *
 * @author jtanza
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpServer implements Server {
    private static final int SELECTOR_TIME_OUT_MS = 1_000;

    private final int port;
    private final ExecutorService workerPool;
    private final ExecutorService serverThread;
    private final RequestDispatcher requestDispatcher;
    private final ChannelBuffer channelBuffer;

    /**
     * @param requestDispatcher
     * @return
     */
    public static Builder builder(RequestDispatcher requestDispatcher) {
        return new Builder(requestDispatcher);
    }

    /**
     * Opens a {@link ServerSocketChannel} on {@link #port} and
     * serves incoming HTTP requests.
     */
    @Override
    public void serve() {
        serverThread.execute(() -> {
            ServerConnection connection = ServerConnection.openConnection(port);
            while (!Thread.interrupted()) {
                listenAndServe(connection.getSelector());
            }
            connection.close();
        });
    }

    /**
     * Sends interrupts to both our {@link ServerSocketChannel} thread
     * and to our {@link #workerPool}, effectively stopping the servicing
     * of any additional HTTP requests on this {@link HttpServer}.
     *
     * Note, this method <em>does not</em> wait for any active connections to
     * complete before shutting down.
     */
    @Override
    public void stop() {
        workerPool.shutdownNow();
        serverThread.shutdownNow();
    }

    private void listenAndServe(Selector selector) {
        while (selector.isOpen()) {
            try {
                selector.select(SELECTOR_TIME_OUT_MS);
                Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();
                    serviceSocketEvent(selector, key);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void serviceSocketEvent(Selector selector, SelectionKey key) {
        if (!key.isValid()) {
            return;
        }
        if (key.isAcceptable()) {
            registerChannelWithSelector(key, selector);
        } else if (key.isReadable()) {
            readThenWrite(key);
        }
    }

    /**
     * read from a readable {@link SelectionKey} and
     * asynchronously process the data/write our response.
     */
    private void readThenWrite(SelectionKey key) {
        try {
            channelBuffer.readFromChannel(key).ifPresent(read -> {
                Request request = Request.from(read);
                Optional<RequestHandler> handler = requestDispatcher.getHandlerFor(request);
                if (handler.isPresent()) {
                    handleRequestAsync(handler.get(), key, request);
                } else {
                    writeAsync(key, Response.from(NOT_FOUND).build());
                }
            });
        } catch (RequestException e) {
            writeAsync(key, Response.from(e));
        }
    }

    private void handleRequestAsync(RequestHandler handler, SelectionKey key, Request request) {
        CompletableFuture.supplyAsync(() -> augmentedHandlerAction().apply(request, handler), workerPool)
            .thenAccept(response -> write(key, response))
            .thenAccept((c) -> Utils.closeConnection(key));
    }

    private static void write(SelectionKey key, Response response) {
        try {
            if (key.isValid()) {
                ((SocketChannel) key.channel()).write(response.toByteBuffer());
            }
        } catch (IOException e) {
            e.printStackTrace();
            Utils.closeConnection(key);
        }
    }

    private void writeAsync(SelectionKey key, Response response) {
        CompletableFuture.runAsync(() -> write(key, response), workerPool);
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
            Utils.closeConnection(key);
        }
    }

    /**
     * Mutates the {@link Request} with path variables computed with data derived from a
     * {@link RequestHandler} before applying {@link RequestHandler#getAction()}.
     *
     * We delegate this mutation to be performed within the {@link RequestHandler#getAction()}
     * execution context so that we can leverage our {@link HttpServer#workerPool} and keep free
     * our multiplexing thread.
     */
    private static BiFunction<Request, RequestHandler, Response> augmentedHandlerAction() {
        return (request, handler) -> {
            request.setPathVariables(handler);
            return handler.getAction().apply(request);
        };
    }

    /**
     * Builder used for generating {@link HttpServer}s. Default values used in the associated
     * {@link HttpServer} are all defined here.
     */
    public static class Builder {
        private static final int DEFAULT_PORT = 80;
        private static final int DEFAULT_MAX_READ_BYTES = 1024 * 500;
        private static final ExecutorService DEFAULT_THREAD_POOL = Executors.newCachedThreadPool();

        private int port = DEFAULT_PORT;
        private int maxFormSize = DEFAULT_MAX_READ_BYTES;
        private ExecutorService workerPool = DEFAULT_THREAD_POOL;

        private final RequestDispatcher requestDispatcher;

        public Builder(RequestDispatcher requestDispatcher) {
            this.requestDispatcher = requestDispatcher;
        }

        /**
         * @param port to open this {@link HttpServer} on.
         * @return
         */
        public Builder port(int port) {
            this.port = port;
            return this;
        }

        /**
         * @param maxFormSize in bytes.
         * @return
         */
        public Builder maxFormSize(int maxFormSize) {
            this.maxFormSize = maxFormSize;
            return this;
        }

        /**
         * @param workerPool {@link ExecutorService} in which request processing is carried out.
         * @return
         */
        public Builder workerPool(ExecutorService workerPool) {
            this.workerPool = workerPool;
            return this;
        }

        public HttpServer build() {
            return new HttpServer(port, workerPool, Executors.newSingleThreadExecutor(), requestDispatcher, new ChannelBuffer(maxFormSize));
        }
    }

    /**
     * Represents a {@link ServerSocketChannel} and its registered {@link Selector}
     * open on the {@link #port} provided.
     */
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

        void close() {
            try {
                if (selector != null) {
                    selector.close();
                }
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
