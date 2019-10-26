package com.tanza.dashi;

import com.tanza.dashi.lib.Headers;
import com.tanza.dashi.lib.LibConstants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jtanza
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Request {
    private final LibConstants.Method method;
    private final URL url;
    private final Headers headers;
    private final String body;
    private final List<Pair<String, String>> queryParameters;
    private Map<String, String> pathVariables;

    public static Request from(byte[] request) {
        return from(new String(request, StandardCharsets.UTF_8));
    }

    public static Request from(String request) {
        return HttpParser.parseRequest(request);
    }

    /**
     * @param varName Name of path variable name used in {@link RequestHandler#getPath()}.
     * @return The {@link String} value associated with the {@param pathVariable} or
     * <code>null</code> if no such mapping exists.
     *
     * See {@link Request#parsePathVariables(String)} for how mappings are computed.
     */
    public String getPathVariable(@NonNull String varName) {
        if (pathVariables == null || pathVariables.isEmpty()) {
            return null;
        }

        return pathVariables.get(varName);
    }

    /**
     * Extracts URL path variables, mapping the values from slugs provided in
     * {@link RequestHandler#getPath()}.
     *
     * e.g. given:
     * <code>/users/{userId}/orders/{orderId}</code>
     * with a URL path of:
     * <code>/users/123/orders/456</code>
     * will yield:
     * <code>userId:123,orderId:456</code>
     *
     * @param requestHandler
     */
    void parsePathVariables(@NonNull RequestHandler requestHandler) {
        parsePathVariables(requestHandler.getPath());
    }

    //TODO url encoding
    void parsePathVariables(String path) {
        if (StringUtils.isEmpty(path)) {
            return;
        }

        List<String> pathSegments = Arrays.stream(url.getPath().split("/")).map(p -> URLDecoder.decode(p, StandardCharsets.UTF_8)).collect(Collectors.toList());
        String[] variablePathSegments = path.split("/");

        Map<String, String> pathVariables = new HashMap<>();
        for (int i = 0; i < variablePathSegments.length; i++) {
            String segment = variablePathSegments[i];
            if (segment.startsWith("{")) {
                pathVariables.put(extractVarName(segment), pathSegments.get(i));
            }
        }
        this.pathVariables = pathVariables;
    }

    private static String extractVarName(String pathVariable) {
        return pathVariable.substring(1, pathVariable.lastIndexOf("}"));
    }
}
