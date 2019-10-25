package com.tanza.kudu;

import com.tanza.kudu.lib.LibConstants.StatusCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author jtanza
 */
@Getter
@RequiredArgsConstructor
public class RequestException extends RuntimeException {
    static final long serialVersionUID = 1;

    private final StatusCode statusCode;
    private final String body;

    public static RequestException from(StatusCode statusCode) {
        return new RequestException(statusCode, null);
    }
}
