package com.tanza.dashi;

import com.tanza.dashi.Constants.Method;

import java.util.concurrent.TimeUnit;

public class Example {

    public static void main(String[] args) {
        RequestDispatcher requestDispatcher = new RequestDispatcher()
            .addHandler(new RequestHandler(Method.PUT, "/users/{userId}", r -> Response.ok(r.getPathVariable("userId"))))
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
            .addResourcePath("/web");


        Server.builder(requestDispatcher).build().serve();
    }
}
