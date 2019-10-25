package com.tanza.dashi;

import com.tanza.dashi.lib.LibConstants.Method;
import com.tanza.dashi.lib.Headers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.tanza.dashi.lib.LibConstants.Header.CONTENT_LENGTH;
import static com.tanza.dashi.lib.LibConstants.Header.HOST;
import static com.tanza.dashi.lib.LibConstants.Header.TRANSFER_ENCODING;
import static com.tanza.dashi.lib.LibConstants.Message.CRLF;
import static com.tanza.dashi.lib.LibConstants.Message.SP;

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

        URL url = parseUrl(requestLine, headers);
        return new Request(
            requestLine.getMethod(), url,
            headers, body, parseQueryParameters(url)
        );
    }

    private static boolean hasBodyHeader(Headers headers) {
        return headers.containsHeader(CONTENT_LENGTH) || headers.containsHeader(TRANSFER_ENCODING);
    }

    private static URL parseUrl(RequestLine requestLine, Headers headers) {
        try {
            //TODO always http for now
            return new URL("http://" + headers.getValue(HOST) + requestLine.getRequestUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Pair<String, String>> parseQueryParameters(URL url) {
        List<Pair<String, String>> res = new ArrayList<>();
        try {
            List<NameValuePair> nameValuePairs = URLEncodedUtils.parse(url.toURI(), StandardCharsets.UTF_8.name());
            res = nameValuePairs.stream().map(nvp -> Pair.of(nvp.getName(), nvp.getValue())).collect(Collectors.toList());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Getter
    @AllArgsConstructor
    private static class RequestLine {
        private final Method method;
        private final String requestUri;
        private final String httpVersion;

        private static RequestLine from(String[] requestLine) {
            return new RequestLine(Method.from(requestLine[0]), requestLine[1], requestLine[2]);
        }
    }
}
