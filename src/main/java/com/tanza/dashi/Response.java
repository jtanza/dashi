package com.tanza.dashi;

import com.tanza.dashi.Constants.StatusCode;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static com.tanza.dashi.Constants.Header.CONTENT_LENGTH;
import static com.tanza.dashi.Constants.Header.DATE;
import static com.tanza.dashi.Constants.Header.SERVER;
import static com.tanza.dashi.Constants.StatusCode.BAD_REQUEST;
import static com.tanza.dashi.Constants.StatusCode.OK;

/**
 * @author jtanza
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class Response {
    static final String CRLF = "\r\n";

    private static final String VERSION = "HTTP/1.1 ";
    private static final String DASHI_V = "Dashi/0.0.1";
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss O");

    private final StatusCode statusCode;
    private final Headers headers;
    private final Pair<String, Integer> customStatusCode;
    private String body;

    public static Builder ok() {
        return new Builder().statusCode(OK);
    }

    public static Response ok(String body) {
        return new Response(OK, new Headers(), null, body);
    }

    public static Builder badRequest() {
        return new Builder().statusCode(BAD_REQUEST);
    }

    public static Response badRequest(String body) {
        return new Response(BAD_REQUEST, new Headers(), null, body);
    }

    public static Builder from(@NonNull StatusCode statusCode) {
        return new Builder().statusCode(statusCode);
    }

    public static Builder from(@NonNull String reasonPhrase, int code) {
        return new Builder().statusCode(Pair.of(reasonPhrase, code));
    }

    private void addReqResponseHeaders(Headers headers) {
        if (!headers.containsHeader(CONTENT_LENGTH)) {
            headers.addHeader(CONTENT_LENGTH, getContentLength());
        }
        if (!headers.containsHeader(DATE)) {
            headers.addHeader(DATE, Instant.now().atOffset(ZoneOffset.UTC).format(DTF));
        }
        if (!headers.containsHeader(SERVER)) {
            headers.addHeader(SERVER, DASHI_V);
        }
    }

    private String formatStatusLine() {
        return statusCode != null
            ? VERSION + statusCode + CRLF
            : VERSION + formatCustomStatusCode(customStatusCode) + CRLF;
    }

    private String getContentLength() {
        return body == null ? "0" : String.valueOf(body.length() + 1);
    }

    private static String formatCustomStatusCode(Pair<String, Integer> statusCodePair) {
        return statusCodePair.getRight() + " " + statusCodePair.getLeft();
    }

    private String formatBody() {
        return body.endsWith("\n") ? body : body + "\n";
    }

    @Override
    public String toString() {
        addReqResponseHeaders(headers);
        String resp = formatStatusLine() + headers.toString() + CRLF;
        return body == null ? resp : resp + formatBody();
    }

    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(toString().getBytes());
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {
        private StatusCode statusCode;
        private Headers headers = new Headers();
        private Pair<String, Integer> customStatusCode;
        private String body;

        private Builder statusCode(StatusCode statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        private Builder statusCode(Pair<String, Integer> customStatusCode) {
            this.customStatusCode = customStatusCode;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder header(String header, String value) {
            this.headers.addHeader(header, value);
            return this;
        }

        public Response build() {
            return new Response(statusCode, headers, customStatusCode, body);
        }
    }
}
