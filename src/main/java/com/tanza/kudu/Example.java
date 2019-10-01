package com.tanza.kudu;

import com.tanza.kudu.Constants.Method;
import com.tanza.kudu.Constants.StatusCode;

import java.io.IOException;

public class Example {

    public static void main(String[] args) throws IOException {
        RequestDispatch dispatch = new RequestDispatch()
            .addHandler(new Handler(Method.PUT, "/users/123", r -> Response.ok()))
            .addHandler(new Handler(Method.DELETE, "/users/123", r -> Response.badRequest()))
            .addHandler(Handler.asGet("/", r -> Response.ok("FOOBAR")))
            .addHandler(Handler.asGet("/index.html", r -> Response.ok(Utils.getResource("index.html"))))
            .addDefault(Handler.defaultHandler(r -> Response.from(StatusCode.NOT_FOUND)));

        Server server = new Server(dispatch);
        server.serve();
    }
}
