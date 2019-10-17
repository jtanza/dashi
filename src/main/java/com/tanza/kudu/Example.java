package com.tanza.kudu;

import com.tanza.kudu.Constants.Method;
import com.tanza.kudu.Constants.StatusCode;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Example {

    public static void main(String[] args) throws IOException {
        // TODO var segments in urls /users/{id}
        RequestDispatcher requestDispatcher = new RequestDispatcher()
            .addHandler(new RequestHandler(Method.PUT, "/users/{userId}", r -> Response.ok()))
            .addHandler(new RequestHandler(Method.DELETE, "/users/123", r -> Response.badRequest()))
            .addHandler(new RequestHandler("/users/123/orders", r -> Response.ok()))
            .addHandler(new RequestHandler("/foo", r -> Response.ok("FOO")))
            .addHandler(new RequestHandler("/", r -> {
                try {
                    TimeUnit.SECONDS.sleep(8);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return Response.ok("BAR");
            }))
            .addHandler(new RequestHandler("/index.html", r -> Response.ok(Utils.getResource("index.html"))))
            .addDefault(RequestHandler.defaultHandler(r -> Response.from(StatusCode.NOT_FOUND)));

        Server server = new Server(requestDispatcher);
        server.serve();
    }
}
