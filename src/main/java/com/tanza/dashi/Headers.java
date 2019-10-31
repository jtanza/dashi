package com.tanza.dashi;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jtanza
 */
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Headers {
    private static final String CRLF = "\r\n";

    private final Map<String, String> headers;

    public Headers() {
        this.headers = new HashMap<>();
    }

    public void addHeader(String header, String value) {
        headers.put(header, value);
    }

    public String getValue(String header) {
        if (header == null) {
            return null;
        }
        return headers.get(header);
    }

    public boolean containsHeader(String header) {
        return header != null && headers.containsKey(header);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        headers.forEach((k, v) -> builder.append(k).append(": ").append(v).append(CRLF));
        return builder.toString();
    }
}
