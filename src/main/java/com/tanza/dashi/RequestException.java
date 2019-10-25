package com.tanza.dashi;

import com.tanza.dashi.lib.LibConstants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author jtanza
 */
@Getter
@RequiredArgsConstructor
public class RequestException extends RuntimeException {
    static final long serialVersionUID = 1;

    private final LibConstants.StatusCode statusCode;
    private final String body;

    public static RequestException from(LibConstants.StatusCode statusCode) {
        return new RequestException(statusCode, null);
    }
}
