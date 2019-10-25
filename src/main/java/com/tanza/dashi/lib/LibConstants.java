package com.tanza.dashi.lib;

import lombok.NonNull;

/**
 * @author jtanza
 */
public interface LibConstants {

    interface Message {
        String VERSION = "HTTP/1.1 ";
        String CRLF = "\r\n";
        String SP = " ";
        String DASHI_V = "Dashi/0.0.1";
    }

    interface Header {
        String CONTENT_LENGTH = "Content-Length";
        String TRANSFER_ENCODING = "Transfer-Encoding";
        String HOST = "Host";
        String DATE = "Date";
        String SERVER = "Server";
    }

    enum Method {
        GET, POST, DELETE, PUT;

        public static Method from(@NonNull String method) {
            return Enum.valueOf(Method.class, method.trim().toUpperCase());
        }
    }

    enum StatusCode {
        OK("OK", 200), NO_CONTENT("No Content", 204),
        BAD_REQUEST("Bad Request", 400), PAYLOAD_TOO_LARGE("Payload Too Large", 413),
        NOT_FOUND("Not Found", 404),
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
