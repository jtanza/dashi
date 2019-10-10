package com.tanza.kudu;

import com.tanza.kudu.Constants.Method;
import com.tanza.kudu.Constants.StatusCode;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Example {

    public static void main(String[] args) throws IOException {
        RequestHandlers handlers = new RequestHandlers()
            .addHandler(new Handler(Method.PUT, "/users/123", r -> Response.ok()))
            .addHandler(new Handler(Method.DELETE, "/users/123", r -> Response.badRequest()))
            .addHandler(Handler.asGet("/foo", r -> Response.ok("FOO")))
            .addHandler(Handler.asGet("/", r -> {
                try {
                    TimeUnit.SECONDS.sleep(8);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return Response.ok("BAR");
            }))
            .addHandler(Handler.asGet("/index.html", r -> Response.ok(Utils.getResource("index.html"))))
            .addDefault(Handler.defaultHandler(r -> Response.from(StatusCode.NOT_FOUND)));

        Server server = new Server(handlers);
        server.serve();
    }
}
