package com.tanza.dashi.lib;

import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;

import static com.tanza.dashi.lib.LibConstants.Message.CRLF;

/**
 * @author jtanza
 */
@EqualsAndHashCode
public class Headers {
    public static final Headers EMPTY_HEADER = new Headers();

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
