package com.tanza.dashi;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulation of the internal representation of HTTP header fields.
 * Used in both {@link Request} and {@link Response} contexts.
 *
 * @author jtanza
 */
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class Headers {
    static final String CRLF = "\r\n";

    private final Map<String, String> headers;

    Headers() {
        this.headers = new HashMap<>();
    }

    void addHeader(String header, String value) {
        headers.put(header, value);
    }

    String getValue(String header) {
        if (header == null) {
            return null;
        }
        return headers.get(header);
    }

    boolean containsHeader(String header) {
        return header != null && headers.containsKey(header);
    }

    /**
     * @return HTTP compliant representation of header fields
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        headers.forEach((k, v) -> builder.append(k).append(": ").append(v).append(CRLF));
        return builder.toString();
    }
}
