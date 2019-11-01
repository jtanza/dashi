package com.tanza.dashi;

import lombok.NonNull;

/**
 * Common constants used in interacting with HTTP
 * request/response messages
 *
 * @author jtanza
 */
public interface HttpConstants {

    interface Header {
        String ACCEPT = "Accept";
        String CACHE_CONTROL = "Cache-Control";
        String CONTENT_LENGTH = "Content-Length";
        String COOKIE = "Cookie";
        String DATE = "Date";
        String ETAG = "ETag";
        String HOST = "Host";
        String SERVER = "Server";
        String TRANSFER_ENCODING = "Transfer-Encoding";
    }

    enum Method {
        DELETE, GET, HEAD, OPTIONS, PATCH, POST, PUT;

        public static Method from(@NonNull String method) {
            return Enum.valueOf(Method.class, method.trim().toUpperCase());
        }
    }

    enum StatusCode {
        OK("OK", 200), CREATED("Created", 201), NO_CONTENT("No Content", 204),
        MOVED_PERMANENTLY("Moved Permanently", 301), FOUND("Found", 302), SEE_OTHER("See Other", 303),
        BAD_REQUEST("Bad Request", 400), PAYLOAD_TOO_LARGE("Payload Too Large", 413), NOT_FOUND("Not Found", 404),
        INTERNAL_SERVER_ERROR("Internal Server Error", 500);

        private final String reasonPhrase;
        private final int code;

        StatusCode(String reasonPhrase, int code) {
            this.reasonPhrase = reasonPhrase;
            this.code = code;
        }

        @Override
        public String toString() {
            return code + " " + reasonPhrase;
        }
    }
}
