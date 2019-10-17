package com.tanza.kudu;

import com.tanza.kudu.Constants.Method;

import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.net.URL;
import java.util.List;

/**
 * @author jtanza
 */
@Data
public class Request {
    private final Method method;
    private final URL url;
    private final Headers headers;
    private final String body;
    private final List<Pair<String, String>> queryParameters;
    //private Map<String, String> pathVariables;

    public static Request parseRequest(byte[] request) {
        return parseRequest(new String(request));
    }

    public static Request parseRequest(String request) {
        return HttpParser.parseRequest(request);
    }
}
