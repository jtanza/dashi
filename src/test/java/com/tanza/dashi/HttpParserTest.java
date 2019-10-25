package com.tanza.dashi;

import com.tanza.dashi.lib.LibConstants.Header;
import com.tanza.dashi.lib.LibConstants.Method;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author jtanza
 */
public class HttpParserTest {

    @Test
    public void testGet() {
        String requestMessage =
            "GET /docs/index.html HTTP/1.1\r\n" +
            "Host: foo.com\r\n" +
            "User-Agent: curl/7.54.0\r\n" +
            "Accept: */*\r\n\r\n";

        Request request = HttpParser.parseRequest(requestMessage);
        assertEquals("http://foo.com/docs/index.html", request.getUrl().toExternalForm());
        assertNull(request.getBody());
        assertEquals(Method.GET, request.getMethod());
        assertTrue(request.getHeaders().containsHeader(Header.HOST));
    }

    @Test
    public void testPost() {
        String requestMessage =
            "POST / HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "User-Agent: curl/7.54.0\r\n" +
                "Accept: */*\r\n" +
                "Content-Length: 34\r\n" +
                "Content-Type: application/x-www-form-urlencoded\r\n" +
                "\r\n" +
                "{\"key1\":\"value1\", \"key2\":\"value2\"}";

        Request request = HttpParser.parseRequest(requestMessage);

        assertEquals("http://localhost/", request.getUrl().toExternalForm());
        assertNotNull(request.getBody());
        assertEquals("{\"key1\":\"value1\", \"key2\":\"value2\"}", request.getBody());
        assertEquals(Method.POST, request.getMethod());
        assertTrue(request.getHeaders().containsHeader(Header.HOST));
        assertEquals("34", request.getHeaders().getValue(Header.CONTENT_LENGTH));
    }

    @Test
    public void testQueryParams() {
        String requestMessage =
            "GET /resource?query=admin&name=bob;sorted=true HTTP/1.1\r\n" +
                "Host: foo.com\r\n" +
                "User-Agent: curl/7.54.0\r\n" +
                "Accept: */*\r\n\r\n";

        Request request = HttpParser.parseRequest(requestMessage);

        assertEquals("http://foo.com/resource?query=admin&name=bob;sorted=true", request.getUrl().toExternalForm());
        assertEquals(Pair.of("query", "admin"), request.getQueryParameters().get(0));
        assertEquals(Pair.of("name", "bob"), request.getQueryParameters().get(1));
        assertEquals(Pair.of("sorted", "true"), request.getQueryParameters().get(2));
        assertNull(request.getBody());
        assertEquals(Method.GET, request.getMethod());
        assertTrue(request.getHeaders().containsHeader(Header.HOST));
    }
}
