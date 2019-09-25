package com.tanza.kudu;

import com.tanza.kudu.Constants.Method;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import static com.tanza.kudu.Constants.Header.*;
import static com.tanza.kudu.Constants.Message.CRLF;
import static com.tanza.kudu.Constants.Message.SP;

/**
 * @author jtanza
 */
public class HttpParser {

    public static Request parseRequest(String request) {
        String[] messageLines = request.split(CRLF);
        RequestLine requestLine = RequestLine.from(messageLines[0].split(SP));

        int i;
        Headers headers = new Headers();
        for (i = 1; i < messageLines.length; i++) {
            String[] headerLine = messageLines[i].split(": ");
            String header = headerLine[0];

            //CRLF without header marks the end of header section
            if (StringUtils.isEmpty(header)) {
                i++;
                break;
            }

            headers.addHeader(header, headerLine[1]);
        }

        String body = hasBodyHeader(headers)
            ? String.join("", Arrays.copyOfRange(messageLines, i, messageLines.length))
            : null;

        return new Request(
            requestLine.getMethod(), parseUrl(requestLine, headers),
            headers, body
        );
    }

    private static boolean hasBodyHeader(Headers headers) {
        return headers.containsHeader(CONTENT_LENGTH) || headers.containsHeader(TRANSFER_ENCODING);
    }

    private static URL parseUrl(RequestLine requestLine, Headers headers) {
        try {
            return new URL("http://" + headers.getValue(HOST) + requestLine.getRequestUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    private static class RequestLine {
        private final Method method;
        private final String requestUri;
        private final String HttpVersion;

        private static RequestLine from(String[] requestLine) {
            return new RequestLine(Method.from(requestLine[0]), requestLine[1], requestLine[2]);
        }
    }
}
