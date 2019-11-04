package cc.tanza.dashi;

import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

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


        Optional<RequestHandler> rootHandler = dispatch.getHandlerFor(HttpConstants.Method.GET, "/");
        assertTrue(rootHandler.isPresent());
        assertEquals(
            "Handler for / did not return correct response",
            Response.ok("FOOBAR"), rootHandler.get().getAction().apply(any(Request.class))
        );
        assertNotNull(
            "No default handler present",
            dispatch.getHandlerFor(HttpConstants.Method.GET, "/ping")
        );

        dispatch.addHandler(new RequestHandler("/ping", r -> Response.ok("PONG")));
        Optional<RequestHandler> getHandler = dispatch.getHandlerFor(HttpConstants.Method.GET, "/ping");
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
            .addHandler(new RequestHandler(HttpConstants.Method.PUT,    userResource, r -> Response.ok().build()))
            .addHandler(new RequestHandler(HttpConstants.Method.DELETE, userResource, r -> Response.from(HttpConstants.StatusCode.NOT_FOUND).build()));

        Optional<RequestHandler> userResourcePutHandler = dispatch.getHandlerFor(HttpConstants.Method.PUT, userResource);
        assertTrue(userResourcePutHandler.isPresent());
        assertEquals(
            "PUT Handler for " + dispatch + " did not return correct response",
            Response.ok().build(), userResourcePutHandler.get().getAction().apply(any(Request.class))
        );

        Optional<RequestHandler> userResourceDeleteHandler = dispatch.getHandlerFor(HttpConstants.Method.DELETE, userResource);
        assertTrue(userResourceDeleteHandler.isPresent());
        Assert.assertEquals(
            "DELETE Handler for " + dispatch + " did not return correct response",
            Response.from(HttpConstants.StatusCode.NOT_FOUND).build(), userResourceDeleteHandler.get().getAction().apply(any(Request.class))
        );
    }

    @Test
    public void testPathsWithSlugs() {
        {
            RequestDispatcher dispatch = new RequestDispatcher()
                .addHandler(new RequestHandler(HttpConstants.Method.GET, "/users/{userId}/orders/{orderId}", r -> Response.ok().build()));

            Optional<RequestHandler> handler = dispatch.getHandlerFor(HttpConstants.Method.GET, "/users/123/orders/456");
            assertTrue(handler.isPresent());
            assertEquals(Response.ok().build(), handler.get().getAction().apply(any(Request.class)));
        }

        {
            RequestDispatcher dispatch = new RequestDispatcher()
                .addHandler(new RequestHandler(HttpConstants.Method.PUT, "/users/{userId}/orders/{orderId}", r -> Response.ok().build()))
                .addHandler(new RequestHandler(HttpConstants.Method.PUT, "/users/project/orders/{orderId}", r -> Response.from(HttpConstants.StatusCode.NO_CONTENT).build()));

            Optional<RequestHandler> handler = dispatch.getHandlerFor(HttpConstants.Method.GET, "/users/123/orders/456");
            assertTrue(handler.isPresent());
            assertEquals(Response.ok().build(), handler.get().getAction().apply(any(Request.class)));

            Optional<RequestHandler> projectHandler = dispatch.getHandlerFor(HttpConstants.Method.GET, "/users/project/orders/456");
            assertTrue(projectHandler.isPresent());
            Assert.assertEquals(Response.from(HttpConstants.StatusCode.NO_CONTENT).build(), projectHandler.get().getAction().apply(any(Request.class)));
        }

        {
            RequestDispatcher dispatch = new RequestDispatcher()
                .addHandler(new RequestHandler(HttpConstants.Method.PUT, "/index/{endpoint}/{type}/{id}", r -> Response.ok().build()))
                .addHandler(new RequestHandler(HttpConstants.Method.PUT, "/index/pages/{type}/{id}", r -> Response.ok("PAGES")));

            Optional<RequestHandler> handler = dispatch.getHandlerFor(HttpConstants.Method.GET, "/index/page/blue/456");
            assertTrue(handler.isPresent());
            assertEquals(Response.ok().build(), handler.get().getAction().apply(any(Request.class)));

            Optional<RequestHandler> handlerTwo = dispatch.getHandlerFor(HttpConstants.Method.GET, "/index/pages/black/789");
            assertTrue(handlerTwo.isPresent());
            assertEquals(Response.ok("PAGES"), handlerTwo.get().getAction().apply(any(Request.class)));

        }
    }
}