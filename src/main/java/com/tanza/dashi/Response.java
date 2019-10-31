package com.tanza.dashi;

import com.tanza.dashi.LibConstants.StatusCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static com.tanza.dashi.LibConstants.Header.CONTENT_LENGTH;
import static com.tanza.dashi.LibConstants.Header.DATE;
import static com.tanza.dashi.LibConstants.Header.SERVER;
import static com.tanza.dashi.LibConstants.StatusCode.BAD_REQUEST;
import static com.tanza.dashi.LibConstants.StatusCode.OK;

/**
 * @author jtanza
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class Response {
    private static final String VERSION = "HTTP/1.1 ";
    private static final String DASHI_V = "Dashi/0.0.1";
    private static final String CRLF = "\r\n";
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss O");

    private final StatusCode statusCode;
    private final Headers headers;
    private String body;

    public static Builder ok() {
        return new Builder(OK);
    }

    public static Response ok(String body) {
        return new Response(OK, new Headers(), body);
    }

    public static Builder badRequest() {
        return new Builder(BAD_REQUEST);
    }

    public static Response badRequest(String body) {
        return new Response(BAD_REQUEST, new Headers(), body);
    }

    public static Builder from(StatusCode statusCode) {
        return new Builder(statusCode);
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

    private String getContentLength() {
        return body == null ? "0" : String.valueOf(body.length() + 1);
    }

    private String formatStatusLine() {
        return VERSION + statusCode + CRLF;
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

    @RequiredArgsConstructor
    public static class Builder {
        private final StatusCode statusCode;
        private final Headers headers = new Headers();
        private String body;

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder header(String header, String value) {
            this.headers.addHeader(header, value);
            return this;
        }

        public Response build() {
            return new Response(statusCode, headers, body);
        }
    }
}
