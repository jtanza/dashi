package com.tanza.kudu;

import com.tanza.kudu.Constants.Method;

import org.junit.Test;

import org.mockito.ArgumentMatchers;

import static com.tanza.kudu.Constants.StatusCode.NOT_FOUND;
import static org.junit.Assert.*;

/**
 * @author jtanza
 */
public class RequestDispatchTest {

    @Test
    public void testDispatch() {
        RequestDispatch dispatch = new RequestDispatch()
            .addHandler(Handler.asGet("/", r -> Response.ok("FOOBAR")))
            .addHandler(Handler.asGet("/query", r -> Response.ok("BAZBUCK")))
            .addDefault(Handler.defaultHandler(r -> Response.badRequest()));

        assertEquals(
            "Handler for / did not return correct response",
            Response.ok("FOOBAR"), dispatch.handlerFor(Method.GET, "/").orElseThrow().getAction().apply(ArgumentMatchers.any(Request.class))
        );
        assertNotNull(
            "No default handler present",
            dispatch.handlerFor(Method.GET, "/ping")
        );
        assertEquals(
            "Handler for /ping did not return correct response",
            Response.badRequest(), dispatch.handlerFor(Method.GET, "/ping").orElseThrow().getAction().apply(ArgumentMatchers.any(Request.class))
        );

        dispatch.addDefault(Handler.asGet("/ping", r -> Response.ok("PONG")));
        assertEquals(
            "Addition of handler not recognized",
            Response.ok("PONG"), dispatch.handlerFor(Method.GET, "/ping").orElseThrow().getAction().apply(ArgumentMatchers.any(Request.class))
        );
    }

    @Test
    public void testRequestMethods() {
        System.out.println(Response.ok().toString());
        final String userResource = "/users/12345";

        RequestDispatch dispatch = new RequestDispatch()
            .addHandler(new Handler(Method.PUT,    userResource, r -> Response.ok()))
            .addHandler(new Handler(Method.DELETE, userResource, r -> Response.from(NOT_FOUND)));

        assertEquals(
            "PUT Handler for " + dispatch + " did not return correct response",
            Response.ok(), dispatch.handlerFor(Method.PUT, userResource).orElseThrow().getAction().apply(ArgumentMatchers.any(Request.class))
        );

        assertEquals(
            "DELETE Handler for " + dispatch + " did not return correct response",
            Response.from(NOT_FOUND), dispatch.handlerFor(Method.DELETE, userResource).orElseThrow().getAction().apply(ArgumentMatchers.any(Request.class))
        );

    }
}