package com.tanza.dashi;

import com.tanza.dashi.HttpConstants.Method;

import org.junit.Test;

import java.util.Optional;

import static com.tanza.dashi.HttpConstants.StatusCode.NOT_FOUND;
import static com.tanza.dashi.HttpConstants.StatusCode.NO_CONTENT;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;

/**
 * @author jtanza
 */
public class RequestDispatcherTest {

    @Test
    public void testDispatch() {
        RequestDispatcher dispatch = new RequestDispatcher()
            .addHandler(new RequestHandler("/", r -> Response.ok("FOOBAR")))
            .addHandler(new RequestHandler("/query", r -> Response.ok("BAZBUCK")));


        Optional<RequestHandler> rootHandler = dispatch.getHandlerFor(Method.GET, "/");
        assertTrue(rootHandler.isPresent());
        assertEquals(
            "Handler for / did not return correct response",
            Response.ok("FOOBAR"), rootHandler.get().getAction().apply(any(Request.class))
        );
        assertNotNull(
            "No default handler present",
            dispatch.getHandlerFor(Method.GET, "/ping")
        );

        dispatch.addHandler(new RequestHandler("/ping", r -> Response.ok("PONG")));
        Optional<RequestHandler> getHandler = dispatch.getHandlerFor(Method.GET, "/ping");
        assertTrue(getHandler.isPresent());
        assertEquals(
            "Handler for /ping did not return correct response",
            Response.ok("PONG"), getHandler.get().getAction().apply(any(Request.class))
        );
    }

    @Test
    public void testRequestMethods() {
        final String userResource = "/users/12345";

        RequestDispatcher dispatch = new RequestDispatcher()
            .addHandler(new RequestHandler(Method.PUT,    userResource, r -> Response.ok().build()))
            .addHandler(new RequestHandler(Method.DELETE, userResource, r -> Response.from(NOT_FOUND).build()));

        Optional<RequestHandler> userResourcePutHandler = dispatch.getHandlerFor(Method.PUT, userResource);
        assertTrue(userResourcePutHandler.isPresent());
        assertEquals(
            "PUT Handler for " + dispatch + " did not return correct response",
            Response.ok().build(), userResourcePutHandler.get().getAction().apply(any(Request.class))
        );

        Optional<RequestHandler> userResourceDeleteHandler = dispatch.getHandlerFor(Method.DELETE, userResource);
        assertTrue(userResourceDeleteHandler.isPresent());
        assertEquals(
            "DELETE Handler for " + dispatch + " did not return correct response",
            Response.from(NOT_FOUND).build(), userResourceDeleteHandler.get().getAction().apply(any(Request.class))
        );
    }

    @Test
    public void testPathsWithSlugs() {
        {
            RequestDispatcher dispatch = new RequestDispatcher()
                .addHandler(new RequestHandler(Method.GET, "/users/{userId}/orders/{orderId}", r -> Response.ok().build()));

            Optional<RequestHandler> handler = dispatch.getHandlerFor(Method.GET, "/users/123/orders/456");
            assertTrue(handler.isPresent());
            assertEquals(Response.ok().build(), handler.get().getAction().apply(any(Request.class)));
        }

        {
            RequestDispatcher dispatch = new RequestDispatcher()
                .addHandler(new RequestHandler(Method.PUT, "/users/{userId}/orders/{orderId}", r -> Response.ok().build()))
                .addHandler(new RequestHandler(Method.PUT, "/users/project/orders/{orderId}",  r -> Response.from(NO_CONTENT).build()));

            Optional<RequestHandler> handler = dispatch.getHandlerFor(Method.GET, "/users/123/orders/456");
            assertTrue(handler.isPresent());
            assertEquals(Response.ok().build(), handler.get().getAction().apply(any(Request.class)));

            Optional<RequestHandler> projectHandler = dispatch.getHandlerFor(Method.GET, "/users/project/orders/456");
            assertTrue(projectHandler.isPresent());
            assertEquals(Response.from(NO_CONTENT).build(), projectHandler.get().getAction().apply(any(Request.class)));
        }

        {
            RequestDispatcher dispatch = new RequestDispatcher()
                .addHandler(new RequestHandler(Method.PUT, "/index/{endpoint}/{type}/{id}", r -> Response.ok().build()))
                .addHandler(new RequestHandler(Method.PUT, "/index/pages/{type}/{id}",      r -> Response.ok("PAGES")));

            Optional<RequestHandler> handler = dispatch.getHandlerFor(Method.GET, "/index/page/blue/456");
            assertTrue(handler.isPresent());
            assertEquals(Response.ok().build(), handler.get().getAction().apply(any(Request.class)));

            Optional<RequestHandler> handlerTwo = dispatch.getHandlerFor(Method.GET, "/index/pages/black/789");
            assertTrue(handlerTwo.isPresent());
            assertEquals(Response.ok("PAGES"), handlerTwo.get().getAction().apply(any(Request.class)));

        }
    }
}