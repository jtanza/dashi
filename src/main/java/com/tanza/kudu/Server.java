package com.tanza.kudu;

import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author jtanza
 */

public class Server {
    private static final int DEFAULT_PORT = 80;

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
        ServerSocket socket = new ServerSocket(port);
        System.out.println("Listening on port " + port + "...\n");

        while (true) {
            Socket clientSocket = socket.accept();
            SocketBuffer socketBuffer = new SocketBuffer(clientSocket);
            try {
                String requestStr = socketBuffer.read();
                System.out.println(requestStr);
                Request request = Request.from(requestStr);

                Function<Request, Response> handler = dispatch.handlerFor(request.getUrl().getPath());
                if (!Objects.isNull(handler)) {
                    Response response = handler.apply(request);
                    socketBuffer.write(response.toString());
                }
            } catch (RequestException e) {
                socketBuffer.write(Response.from(e).toString());
            }
            socketBuffer.close();
        }
    }
}
