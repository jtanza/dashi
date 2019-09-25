package com.tanza.kudu;

import com.tanza.kudu.Constants.Method;

import lombok.Data;

import java.net.URL;

/**
 * @author jtanza
 */
@Data
public class Request {
    private final Method method;
    private final URL url;
    private final Headers headers;
    private final String body;

    public static Request from(String request) {
        return HttpParser.parseRequest(request);
    }
}
