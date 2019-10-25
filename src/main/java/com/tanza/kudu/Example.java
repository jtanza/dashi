package com.tanza.kudu;

import com.tanza.kudu.lib.LibConstants.Method;
import com.tanza.kudu.lib.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Example {

    public static void main(String[] args) {

        RequestDispatcher requestDispatcher = RequestDispatcher.builder()
            .withHandler(new RequestHandler(Method.PUT, "/users/{userId}", r -> Response.ok()))
            .withHandler(new RequestHandler(Method.DELETE, "/users/123", r -> Response.badRequest()))
            .withHandler(new RequestHandler("/users/123/orders", r -> Response.ok()))
            .withHandler(new RequestHandler("/foo", r -> Response.ok("FOO")))
            .withHandler(new RequestHandler("/", r -> {
                try {
                    TimeUnit.SECONDS.sleep(8);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return Response.ok("BAR");
            }))
            .withHandler(new RequestHandler("/index.html", r -> {
                try {
                    return Response.ok(Utils.getResource("index.html"));
                } catch (IOException e) {
                    return Response.from(e);
                }
            }))
            .build();

        Server.builder(requestDispatcher).port(1024).build().serve();
    }
}
