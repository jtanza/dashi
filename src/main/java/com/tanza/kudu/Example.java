package com.tanza.kudu;

import java.io.IOException;

public class Example {
    private static final String QUERY_PATH = "/query";

    public static void main(String[] args) throws IOException {
        RequestDispatch dispatch = new RequestDispatch()
            .addHandler("/", r -> Response.ok("FOOBAR"))
            .addHandler(QUERY_PATH, r -> Response.ok("BAZBUCK"))
            .addDefault(r -> Response.badRequest());

        Server server = new Server(dispatch);
        server.serve();
    }
}
