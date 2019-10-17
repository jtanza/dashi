package com.tanza.kudu;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author jtanza
 */
public class RequestTest {

    @Test
    public void testPathVariables() {
        Request request = Request.from(
            "GET /users/123/orders/456 HTTP/1.1\r\n" +
                "Host: localhost:1024\r\n" +
                "User-Agent: curl/7.54.0\r\n" +
                "Accept: */*\r\n" +
                "\r\n\r\n"
        );

        Map<String, String> pathVariables = request.parsePathVariables("/users/{userId}/orders/{orderId}");
        assertNotNull(pathVariables);
        assertEquals("123", pathVariables.get("userId"));
        assertEquals("456", pathVariables.get("orderId"));
    }
}