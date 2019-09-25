package com.tanza.kudu;

import java.util.HashMap;
import java.util.Map;

import static com.tanza.kudu.Constants.Message.*;

/**
 * @author jtanza
 */
public class Headers {
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
        builder.append(CRLF);
        return builder.toString();
    }
}
