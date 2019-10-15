package com.tanza.kudu;

import com.tanza.kudu.Constants.StatusCode;
import lombok.EqualsAndHashCode;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static com.tanza.kudu.Constants.Header.CONTENT_LENGTH;
import static com.tanza.kudu.Constants.Header.DATE;
import static com.tanza.kudu.Constants.Header.SERVER;
import static com.tanza.kudu.Constants.Message.CRLF;
import static com.tanza.kudu.Constants.Message.VERSION;
import static com.tanza.kudu.Constants.StatusCode.BAD_REQUEST;
import static com.tanza.kudu.Constants.StatusCode.OK;

/**
 * @author jtanza
 */
@EqualsAndHashCode
public class Response {
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss O");

    private final StatusCode statusCode;
    private final Headers headers;
    private final String body;

    public Response(StatusCode statusCode, Headers headers, String body) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

    public static Response from(RequestException exception) {
        return new Response(exception.getStatusCode(), new Headers(), null);
    }

    public static Response from(StatusCode statusCode) {
        return new Response(statusCode, Headers.EMPTY_HEADER, null);
    }

    public static Response ok(String body) {
        return new Response(OK, Headers.EMPTY_HEADER, body);
    }

    public static Response ok() {
        return new Response(OK, new Headers(), null);
    }

    public static Response badRequest() {
        return new Response(BAD_REQUEST, Headers.EMPTY_HEADER, null);
    }

    private void addReqResponseHeaders(Headers headers) {
        if (!headers.containsHeader(CONTENT_LENGTH)) {
            headers.addHeader(CONTENT_LENGTH, getContentLength());
        }
        if (!headers.containsHeader(DATE)) {
            headers.addHeader(DATE, Instant.now().atOffset(ZoneOffset.UTC).format(DTF));
        }
        if (!headers.containsHeader(SERVER)) {
            headers.addHeader(SERVER, "Kudu/0.0.1");
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
}
