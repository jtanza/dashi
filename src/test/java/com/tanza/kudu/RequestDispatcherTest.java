package com.tanza.kudu;

import com.tanza.kudu.lib.LibConstants.Method;

import com.tanza.kudu.lib.Response;
import org.junit.Test;

import org.mockito.ArgumentMatchers;

import static com.tanza.kudu.lib.LibConstants.StatusCode.NOT_FOUND;
import static org.junit.Assert.*;

/**
 * @author jtanza
 */
public class RequestDispatcherTest {

    @Test
    public void testDispatch() {
        RequestDispatcher dispatch = RequestDispatcher.builder()
            .withHandler(new RequestHandler("/", r -> Response.ok("FOOBAR")))
            .withHandler(new RequestHandler("/query", r -> Response.ok("BAZBUCK")))
            .build();

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
            Response.from(NOT_FOUND), dispatch.getHandlerFor(Method.GET, "/ping").orElseThrow().getAction().apply(ArgumentMatchers.any(Request.class))
        );
    }

    @Test
    public void testRequestMethods() {
        System.out.println(Response.ok().toString());
        final String userResource = "/users/12345";

        RequestDispatcher dispatch = RequestDispatcher.builder()
            .withHandler(new RequestHandler(Method.PUT,    userResource, r -> Response.ok()))
            .withHandler(new RequestHandler(Method.DELETE, userResource, r -> Response.from(NOT_FOUND)))
            .build();

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