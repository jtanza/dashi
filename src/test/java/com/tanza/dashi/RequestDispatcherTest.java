package com.tanza.dashi;

import com.tanza.dashi.lib.LibConstants.Method;
import com.tanza.dashi.lib.Response;

import org.junit.Test;
import org.mockito.ArgumentMatchers;

import java.util.Optional;

import static com.tanza.dashi.lib.LibConstants.StatusCode.NOT_FOUND;
import static com.tanza.dashi.lib.LibConstants.StatusCode.NO_CONTENT;
import static org.junit.Assert.*;

/**
 * @author jtanza
 */
public class RequestDispatcherTest {

    @Test
    public void testDispatch() {
        RequestDispatcher dispatch = new RequestDispatcher()
            .addHandler(new RequestHandler("/", r -> Response.ok("FOOBAR")))
            .addHandler(new RequestHandler("/query", r -> Response.ok("BAZBUCK")));

        assertEquals(
            "Handler for / did not return correct response",
            Response.ok("FOOBAR"), dispatch.getHandlerFor(Method.GET, "/").orElseThrow().getAction().apply(ArgumentMatchers.any(Request.class))
        );
        assertNotNull(
            "No default handler present",
            dispatch.getHandlerFor(Method.GET, "/ping")
        );

        dispatch.addHandler(new RequestHandler("/ping", r -> Response.ok("PONG")));
        assertEquals(
            "Handler for /ping did not return correct response",
            Response.ok("PONG"), dispatch.getHandlerFor(Method.GET, "/ping").orElseThrow().getAction().apply(ArgumentMatchers.any(Request.class))
        );
    }

    @Test
    public void testRequestMethods() {
        final String userResource = "/users/12345";

        RequestDispatcher dispatch = new RequestDispatcher()
            .addHandler(new RequestHandler(Method.PUT,    userResource, r -> Response.ok().build()))
            .addHandler(new RequestHandler(Method.DELETE, userResource, r -> Response.from(NOT_FOUND).build()));

        assertEquals(
            "PUT Handler for " + dispatch + " did not return correct response",
            Response.ok().build(), dispatch.getHandlerFor(Method.PUT, userResource).orElseThrow().getAction().apply(ArgumentMatchers.any(Request.class))
        );

        assertEquals(
            "DELETE Handler for " + dispatch + " did not return correct response",
            Response.from(NOT_FOUND).build(), dispatch.getHandlerFor(Method.DELETE, userResource).orElseThrow().getAction().apply(ArgumentMatchers.any(Request.class))
        );
    }

    @Test
    public void testPathsWithSlugs() {
        {
            RequestDispatcher dispatch = new RequestDispatcher()
                .addHandler(new RequestHandler(Method.GET, "/users/{userId}/orders/{orderId}", r -> Response.ok().build()));

            Optional<RequestHandler> handler = dispatch.getHandlerFor(Method.GET, "/users/123/orders/456");

            assertTrue(handler.isPresent());
            assertEquals(Response.ok().build(), handler.get().getAction().apply(ArgumentMatchers.any(Request.class)));
        }

        {
            RequestDispatcher dispatch = new RequestDispatcher()
                .addHandler(new RequestHandler(Method.PUT, "/users/{userId}/orders/{orderId}", r -> Response.ok().build()))
                .addHandler(new RequestHandler(Method.PUT, "/users/project/orders/{orderId}",  r -> Response.from(NO_CONTENT).build()));

            Optional<RequestHandler> handler = dispatch.getHandlerFor(Method.GET, "/users/123/orders/456");

            assertTrue(handler.isPresent());
            assertEquals(Response.ok().build(), handler.get().getAction().apply(ArgumentMatchers.any(Request.class)));

            Optional<RequestHandler> projectHandler = dispatch.getHandlerFor(Method.GET, "/users/project/orders/456");

            assertTrue(projectHandler.isPresent());
            assertEquals(Response.from(NO_CONTENT).build(), projectHandler.get().getAction().apply(ArgumentMatchers.any(Request.class)));
        }
    }
}