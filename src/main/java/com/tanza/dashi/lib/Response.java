package com.tanza.dashi.lib;

import com.tanza.dashi.RequestException;
import com.tanza.dashi.lib.LibConstants.StatusCode;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static com.tanza.dashi.lib.LibConstants.Header.CONTENT_LENGTH;
import static com.tanza.dashi.lib.LibConstants.Header.DATE;
import static com.tanza.dashi.lib.LibConstants.Header.SERVER;
import static com.tanza.dashi.lib.LibConstants.Message.CRLF;
import static com.tanza.dashi.lib.LibConstants.Message.DASHI_V;
import static com.tanza.dashi.lib.LibConstants.Message.VERSION;
import static com.tanza.dashi.lib.LibConstants.StatusCode.BAD_REQUEST;
import static com.tanza.dashi.lib.LibConstants.StatusCode.INTERNAL_SERVER_ERROR;
import static com.tanza.dashi.lib.LibConstants.StatusCode.NOT_FOUND;
import static com.tanza.dashi.lib.LibConstants.StatusCode.OK;

/**
 * @author jtanza
 */
@AllArgsConstructor
@EqualsAndHashCode
public class Response {
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

    public static Response notFound() {
        return new Response(NOT_FOUND, new Headers(), null);
    }

    public static Builder from(StatusCode statusCode) {
        return new Builder(statusCode);
    }

    public static Response from(Exception exception) {
        if (exception instanceof RequestException) {
            RequestException re = (RequestException) exception;
            return new Response(re.getStatusCode(), new Headers(), re.getBody());
        }
        return new Response(INTERNAL_SERVER_ERROR, new Headers(), exception.getMessage());
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
