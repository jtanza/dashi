package com.tanza.kudu;

import com.tanza.kudu.Constants.Method;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jtanza
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class Request {
    private final Method method;
    private final URL url;
    private final Headers headers;
    private final String body;
    private final List<Pair<String, String>> queryParameters;

    public static Request from(byte[] request) {
        // TODO default Charset
        return from(new String(request));
    }

    public static Request from(String request) {
        return HttpParser.parseRequest(request);
    }

    public Map<String, String> parsePathVariables(String requestPath) {
        if (StringUtils.isEmpty(requestPath)) {
            return Collections.emptyMap();
        }

        String[] pathSegments = url.getPath().split("/");
        String[] variablePathSegments = requestPath.split("/");
        Map<String, String> res = new HashMap<>();
        for (int i = 0; i < variablePathSegments.length; i++) {
            String segment = variablePathSegments[i];
            if (segment.startsWith("{")) {
                res.put(extractVarName(segment), pathSegments[i]);
            }
        }
        return res;
    }

    private static String extractVarName(String pathVariable) {
        return pathVariable.substring(1, pathVariable.lastIndexOf("}"));
    }
}
