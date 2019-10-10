package com.tanza.kudu;

import lombok.Data;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Optional;

/**
 * @author jtanza
 */
public class Server {
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 1024;
    private static final int TIME_OUT_MS = 1000;

    private final int port;
    private final RequestHandlers handlers;
    private final RequestProcessor requestProcessor;

    public Server(int port, RequestHandlers handlers) {
        this.port = port;
        this.handlers = handlers;
        this.requestProcessor = new RequestProcessor();
    }

    public Server(RequestHandlers handlers) {
        this.port = DEFAULT_PORT;
        this.handlers = handlers;
        this.requestProcessor = new RequestProcessor();
    }

    public void serve() throws IOException {
        Connection connection = Connection.openConnection();
        Selector selector = connection.getSelector();

        while (true) {
            selector.select(TIME_OUT_MS);

            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
                processSocketEvent(selector, key);
            }
        }
    }

    private void acceptConnection(SelectionKey key, Selector selector) {
        registerChannel(key, selector);
    }

    private void write(SelectionKey key) {
        Optional<Response> response = requestProcessor.getResponse(key);
        if (response.isPresent()) {
            try {
                SocketChannel channel = (SocketChannel) key.channel();
                channel.write(response.get().toByteBuffer());
                closeConnection(key);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            key.interestOps(SelectionKey.OP_WRITE);
        }
    }

    private void read(SelectionKey key) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.clear();

        int read;
        try {
            read = ((SocketChannel) key.channel()).read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            closeConnection(key);
            return;
        }

        if (read == -1) {
            closeConnection(key);
            return;
        }

        buffer.flip();
        byte[] data = new byte[read];
        buffer.get(data, 0, read);

        Request request = Request.parseRequest(new String(data));
        Optional<Handler> handler = handlers.handlerFor(request.getMethod(), request.getUrl().getPath());
        handler.ifPresent(h -> requestProcessor.processAsync(key, request, h));

        key.interestOps(SelectionKey.OP_WRITE);
    }

    private static void closeConnection(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        key.cancel();
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processSocketEvent(Selector selector, SelectionKey key) {
        if (!key.isValid()) {
            return;
        }
        if (key.isAcceptable()) {
            acceptConnection(key, selector);
        } else if (key.isReadable()) {
            read(key);
        } else if (key.isWritable()) {
            write(key);
        }
    }

    private static void registerChannel(SelectionKey key, Selector selector) {
        try {
            SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
            if (socketChannel != null) {
                socketChannel.configureBlocking(false);
                socketChannel.register(selector, SelectionKey.OP_READ);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void serve_blocking() throws IOException {
        ServerSocket socket = new ServerSocket(port);
        System.out.println("Listening on port " + port + "...\n");

        while (true) {
            Socket clientSocket = socket.accept(); //blocks
            SocketBuffer socketBuffer = new SocketBuffer(clientSocket);
            try {
                String requestStr = socketBuffer.read();
                System.out.println(requestStr);
                Request request = Request.parseRequest(requestStr);

                Optional<Handler> handler = handlers.handlerFor(request.getMethod(), request.getUrl().getPath());

                if (handler.isPresent()) {
                    Response response = handler.get().getAction().apply(request);
                    socketBuffer.write(response.toString());
                }
            } catch (RequestException e) {
                socketBuffer.write(Response.from(e).toString());
            }
            socketBuffer.close();
        }
    }

    @Data
    private static class Connection {
        private final ServerSocketChannel serverSocket;
        private final Selector selector;

        static Connection openConnection() {
            return openConnection(DEFAULT_HOST, DEFAULT_PORT);
        }

        static Connection openConnection(String host, int port) {
            try {
                ServerSocketChannel serverSocket = ServerSocketChannel.open();
                serverSocket.configureBlocking(false);
                serverSocket.socket().bind(new InetSocketAddress(host, port));

                Selector selector = Selector.open();
                serverSocket.register(selector, SelectionKey.OP_ACCEPT);

                return new Connection(serverSocket, selector);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
