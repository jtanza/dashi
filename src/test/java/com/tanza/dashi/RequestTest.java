package com.tanza.dashi;

import com.tanza.dashi.HttpConstants.Method;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author jtanza
 */
public class RequestTest {

    private static RequestHandler stubRequestHandler(String path) {
        return new RequestHandler(path, r -> Response.ok().build());
    }

    @Test
    public void testPathVariables() {
        {
            Request request = Request.from(
                "GET /users/123/orders/456 HTTP/1.1\r\n" +
                    "Host: localhost:1024\r\n" +
                    "User-Agent: curl/7.54.0\r\n" +
                    "Accept: */*\r\n" +
                    "\r\n\r\n"
            );

            request.setPathVariables(stubRequestHandler("/users/{userId}/orders/{orderId}"));
            assertEquals("123", request.getPathVariable("userId"));
            assertEquals("456", request.getPathVariable("orderId"));
        }

        {
            Request request = Request.from(
                "GET /users/bob/post/456 HTTP/1.1\r\n" +
                    "Host: localhost:1024\r\n" +
                    "User-Agent: curl/7.54.0\r\n" +
                    "Accept: */*\r\n" +
                    "\r\n\r\n"
            );

            RequestHandler handler = new RequestHandler(
                Method.PUT, "/users/{userName}/post/{postId}", r -> Response.ok().build()
            );

            request.setPathVariables(handler);
            assertEquals("bob", request.getPathVariable("userName"));
            assertEquals("456", request.getPathVariable("postId"));
        }

        {
            Request request = Request.from(
                "GET /users/all HTTP/1.1\r\n" +
                    "Host: localhost:1024\r\n" +
                    "User-Agent: curl/7.54.0\r\n" +
                    "Accept: */*\r\n" +
                    "\r\n\r\n"
            );

            RequestHandler handler = new RequestHandler(
                Method.PUT, "/users/all", r -> Response.ok().build()
            );

            request.setPathVariables(handler);
            assertNull("bob", request.getPathVariable("userName"));
        }
    }
}