package com.tanza.dashi;

import com.tanza.dashi.HttpConstants.Method;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Encapsulation of the representation of HTTP request messages.
 *
 * @author jtanza
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Request {
    private final Method method;
    private final URL url;
    private final Headers headers;
    private final String body;
    private final List<Pair<String, String>> queryParameters;
    private Map<String, String> pathVariables;

    /**
     * @param request byte representation of an HTTP request message
     * @return a fully formed {@link Request}.
     */
    public static Request from(byte[] request) {
        return from(new String(request, StandardCharsets.UTF_8));
    }

    /**
     * @param request {@link String} representation of an HTTP request message
     * @return a fully formed {@link Request}.
     */
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
        return pathVariables == null ? null : pathVariables.get(varName);
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
    void setPathVariables(@NonNull RequestHandler requestHandler) {
        this.pathVariables = parsePathVariables(requestHandler.getPath());
    }

    private Map<String, String> parsePathVariables(String path) {
        if (StringUtils.isEmpty(path)) {
            return Collections.emptyMap();
        }

        List<String> pathSegments = Arrays.stream(url.getPath().split("/")).map(Request::decodeUrl).collect(Collectors.toList());
        String[] variablePathSegments = path.split("/");

        Map<String, String> pathVariables = new HashMap<>();
        for (int i = 0; i < variablePathSegments.length; i++) {
            String segment = variablePathSegments[i];
            if (segment.startsWith("{")) {
                pathVariables.put(extractVarName(segment), pathSegments.get(i));
            }
        }
        return pathVariables;
    }

    private static String extractVarName(String pathVariable) {
        return pathVariable.substring(1, pathVariable.lastIndexOf("}"));
    }

    private static String decodeUrl(String url) {
        try {
            return URLDecoder.decode(url, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
