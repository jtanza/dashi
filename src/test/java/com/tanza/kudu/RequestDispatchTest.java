package com.tanza.kudu;

import org.junit.Test;

import org.mockito.ArgumentMatchers;

import static org.junit.Assert.*;

/**
 * @author jtanza
 */
public class RequestDispatchTest {

    @Test
    public void testDispatch() {
        RequestDispatch dispatch = new RequestDispatch()
            .addHandler(new Handler("/", r -> Response.ok("FOOBAR")))
            .addHandler(new Handler("/query", r -> Response.ok("BAZBUCK")))
            .addDefault(Handler.defaultHandler(r -> Response.badRequest()));

        assertEquals(Response.ok("FOOBAR"), dispatch.handlerFor("/").orElseThrow().getAction().apply(ArgumentMatchers.any(Request.class)));
        assertNotNull("No default handler present", dispatch.handlerFor("/ping"));
        assertEquals(Response.badRequest(), dispatch.handlerFor("/ping").orElseThrow().getAction().apply(ArgumentMatchers.any(Request.class)));

        dispatch.addDefault(new Handler("/ping", r -> Response.ok("PONG")));
        assertEquals("Addition of handler not recognized", Response.ok("PONG"), dispatch.handlerFor("/ping").orElseThrow().getAction().apply(ArgumentMatchers.any(Request.class)));
    }
}