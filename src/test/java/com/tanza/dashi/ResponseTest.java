package com.tanza.dashi;

import com.tanza.dashi.HttpConstants.StatusCode;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author jtanza
 */
public class ResponseTest {
    @Test
    public void testOkStatusCodeFormat() {
        {
            Response resp = Response.ok().build();
            String[] splitResponse = resp.toString().split(Headers.CRLF);

            assertEquals(4, splitResponse.length);
            assertEquals("HTTP/1.1 200 OK", splitResponse[0]);
            assertEquals("Content-Length: 0", splitResponse[2]);
        }

        {
            Response resp = Response.ok("FOO_BAR");
            String[] splitResponse = resp.toString().split(Headers.CRLF);

            assertEquals(6, splitResponse.length);
            assertEquals("HTTP/1.1 200 OK", splitResponse[0]);
            assertEquals("Content-Length: 8", splitResponse[2]);
            assertEquals("FOO_BAR\n", splitResponse[5]);
        }

    }

    @Test
    public void testFromStatusCodeFormat() {
        {
            Response resp = Response.from(StatusCode.INTERNAL_SERVER_ERROR).build();
            String[] splitResponse = resp.toString().split(Headers.CRLF);

            assertEquals(4, splitResponse.length);
            assertEquals("HTTP/1.1 500 Internal Server Error", splitResponse[0]);
            assertEquals("Content-Length: 0", splitResponse[2]);
        }

        {
            Response resp = Response.from(StatusCode.INTERNAL_SERVER_ERROR).body("BUZZ").build();
            String[] splitResponse = resp.toString().split(Headers.CRLF);

            assertEquals(6, splitResponse.length);
            assertEquals("HTTP/1.1 500 Internal Server Error", splitResponse[0]);
            assertEquals("Content-Length: 5", splitResponse[2]);
            assertEquals("BUZZ\n", splitResponse[5]);
        }
    }

    @Test
    public void testFromCustomStatusCodeFormat() {
        {
            Response resp = Response.from("I'm a teapot", 418).build();
            String[] splitResponse = resp.toString().split(Headers.CRLF);

            assertEquals(4, splitResponse.length);
            assertEquals("HTTP/1.1 418 I'm a teapot", splitResponse[0]);
            assertEquals("Content-Length: 0", splitResponse[2]);
        }

        {
            Response resp = Response.from("Too Early", 425).body("ZZZ").build();
            String[] splitResponse = resp.toString().split(Headers.CRLF);

            assertEquals(6, splitResponse.length);
            assertEquals("HTTP/1.1 425 Too Early", splitResponse[0]);
            assertEquals("Content-Length: 4", splitResponse[2]);
            assertEquals("ZZZ\n", splitResponse[5]);
        }
    }

    //TODO
    @Test
    public void testHeaders() {

    }
}
