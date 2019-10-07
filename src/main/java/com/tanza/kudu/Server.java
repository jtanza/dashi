package com.tanza.kudu;

import org.apache.commons.lang3.tuple.Pair;

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
    private final RequestDispatch dispatch;

    public Server(int port, RequestDispatch dispatch) {
        this.port = port;
        this.dispatch = dispatch;
    }

    public Server(RequestDispatch dispatch) {
        this.port = DEFAULT_PORT;
        this.dispatch = dispatch;
    }

    public void serve() throws IOException {
        Pair<ServerSocketChannel, Selector> connection = openServerSocket();
        Selector selector = connection.getValue();

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

    private static void accept(SelectionKey key, Selector selector) {
        ServerSocketChannel channel = (ServerSocketChannel) key.channel();

        // accept connection, register w/ selector
        try {
            SocketChannel socketChannel = channel.accept();
            if (socketChannel != null) {
                socketChannel.configureBlocking(false);
                socketChannel.register(selector, SelectionKey.OP_READ);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void write(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            channel.write(ByteBuffer.wrap(Response.ok("OK").toString().getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        key.interestOps(SelectionKey.OP_READ);
    }

    private static void read(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.clear();

        int read;
        try {
            read = channel.read(buffer);
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

        System.out.println(new String(data));
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

    private static Pair<ServerSocketChannel, Selector> openServerSocket() {
        try {
            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.configureBlocking(false);
            serverSocket.socket().bind(new InetSocketAddress(DEFAULT_HOST, DEFAULT_PORT));

            Selector selector = Selector.open();
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);

            return Pair.of(serverSocket, selector);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void processSocketEvent(Selector selector, SelectionKey key) {
        if (!key.isValid()) {
            return;
        }
        if (key.isAcceptable()) {
            accept(key, selector);
        } else if (key.isReadable()) {
            read(key);
        } else if (key.isWritable()) {
            write(key);
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
                Request request = Request.from(requestStr);

                Optional<Handler> handler = dispatch.handlerFor(request.getMethod(), request.getUrl().getPath());

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
}
