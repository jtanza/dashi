package com.tanza.kudu;

import com.tanza.kudu.Constants.StatusCode;

import java.io.IOException;

public class Example {
    private static final String QUERY_PATH = "/query";

    public static void main(String[] args) throws IOException {
        RequestDispatch dispatch = new RequestDispatch()
            .addHandler(new Handler("/", r -> Response.ok("FOOBAR")))
            .addHandler(new Handler("/index.html", r -> Response.ok(Utils.getResource("index.html"))))
            //TODO
            //.addHandler(new Handler("/house/{houseId}/room/{roomId}/door/{doorId}", r -> Response.ok()))
            .addHandler(new Handler(QUERY_PATH, r -> Response.ok("BAZBUCK")))
            .addDefault(Handler.defaultHandler(r -> Response.from(StatusCode.NOT_FOUND)));

        Server server = new Server(dispatch);
        server.serve();
    }
}
