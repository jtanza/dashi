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
            .addHandler("/", r -> Response.ok("FOOBAR"))
            .addHandler("/query", r -> Response.ok("BAZBUCK"))
            .addDefault(r -> Response.badRequest());

        assertEquals(Response.ok("FOOBAR"), dispatch.handlerFor("/").apply(ArgumentMatchers.any(Request.class)));
        assertNotNull("No default handler present", dispatch.handlerFor("/ping"));
        assertEquals(Response.badRequest(), dispatch.handlerFor("/ping").apply(ArgumentMatchers.any(Request.class)));

        dispatch.addHandler("/ping", r -> Response.ok("PONG"));
        assertEquals("Addition of handler not recognized", Response.ok("PONG"), dispatch.handlerFor("/ping").apply(ArgumentMatchers.any(Request.class)));
    }
}