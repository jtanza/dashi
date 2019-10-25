package com.tanza.dashi;

import com.tanza.dashi.lib.LibConstants.Method;
import com.tanza.dashi.lib.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Example {

    public static void main(String[] args) {
        RequestDispatcher requestDispatcher = new RequestDispatcher()
            .addHandler(new RequestHandler(Method.PUT, "/users/{userId}", r -> Response.ok().build()))
            .addHandler(new RequestHandler(Method.DELETE, "/users/123", r -> Response.badRequest().build()))
            .addHandler(new RequestHandler("/users/123/orders", r -> Response.ok().build()))
            .addHandler(new RequestHandler("/foo", r -> Response.ok("FOO")))
            .addHandler(new RequestHandler("/", r -> {
                try {
                    TimeUnit.SECONDS.sleep(8);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return Response.ok("BAR");
            }))
            .addHandler(new RequestHandler("/index.html", r -> {
                try {
                    return Response.ok(Utils.getResource("index.html"));
                } catch (IOException e) {
                    return Response.from(e);
                }
            }));

        Server.builder(requestDispatcher).port(1024).build().serve();
    }
}
