package com.tanza.dashi;

import com.tanza.dashi.LibConstants.StatusCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author jtanza
 */
@Getter
@RequiredArgsConstructor
class RequestException extends Exception {
    static final long serialVersionUID = 1;

    private final StatusCode statusCode;
    private final String body;

    static RequestException from(StatusCode statusCode) {
        return new RequestException(statusCode, null);
    }
}
