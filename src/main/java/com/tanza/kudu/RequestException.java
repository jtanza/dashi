package com.tanza.kudu;

import com.tanza.kudu.Constants.StatusCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author jtanza
 */
@Getter
@AllArgsConstructor
public class RequestException extends Throwable {
    static final long serialVersionUID = 1;

    private final StatusCode statusCode;
    private final String body;

    public static RequestException from(StatusCode statusCode) {
        return new RequestException(statusCode, null);
    }
}
