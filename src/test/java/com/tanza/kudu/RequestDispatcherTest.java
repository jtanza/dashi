package com.tanza.kudu;

import com.tanza.kudu.Constants.Method;

import org.junit.Test;

import org.mockito.ArgumentMatchers;

import static com.tanza.kudu.Constants.StatusCode.NOT_FOUND;
import static org.junit.Assert.*;

/**
 * @author jtanza
 */
public class RequestDispatcherTest {

    @Test
    public void testDispatch() {
        RequestDispatcher dispatch = new RequestDispatcher()
            .addHandler(RequestHandler.asGet("/", r -> Response.ok("FOOBAR")))
            .addHandler(RequestHandler.asGet("/query", r -> Response.ok("BAZBUCK")))
            .addDefault(RequestHandler.defaultHandler(r -> Response.badRequest()));

        assertEquals(
            "Handler for / did not return correct response",
            Response.ok("FOOBAR"), dispatch.getHandlerFor(Method.GET, "/").orElseThrow().getAction().apply(ArgumentMatchers.any(Request.class))
        );
        assertNotNull(
            "No default handler present",
            dispatch.getHandlerFor(Method.GET, "/ping")
        );
        assertEquals(
            "Handler for /ping did not return correct response",
            Response.badRequest(), dispatch.getHandlerFor(Method.GET, "/ping").orElseThrow().getAction().apply(ArgumentMatchers.any(Request.class))
        );

        dispatch.addDefault(RequestHandler.asGet("/ping", r -> Response.ok("PONG")));
        assertEquals(
            "Addition of handler not recognized",
            Response.ok("PONG"), dispatch.getHandlerFor(Method.GET, "/ping").orElseThrow().getAction().apply(ArgumentMatchers.any(Request.class))
        );
    }

    @Test
    public void testRequestMethods() {
        System.out.println(Response.ok().toString());
        final String userResource = "/users/12345";

        RequestDispatcher dispatch = new RequestDispatcher()
            .addHandler(new RequestHandler(Method.PUT,    userResource, r -> Response.ok()))
            .addHandler(new RequestHandler(Method.DELETE, userResource, r -> Response.from(NOT_FOUND)));

        assertEquals(
            "PUT Handler for " + dispatch + " did not return correct response",
            Response.ok(), dispatch.getHandlerFor(Method.PUT, userResource).orElseThrow().getAction().apply(ArgumentMatchers.any(Request.class))
        );

        assertEquals(
            "DELETE Handler for " + dispatch + " did not return correct response",
            Response.from(NOT_FOUND), dispatch.getHandlerFor(Method.DELETE, userResource).orElseThrow().getAction().apply(ArgumentMatchers.any(Request.class))
        );

    }
}