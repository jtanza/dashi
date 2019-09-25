package com.tanza.kudu;

import com.tanza.kudu.Constants.StatusCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author jtanza
 */
@Getter
@AllArgsConstructor
public class RequestException extends RuntimeException {
    static final long serialVersionUID = 1;

    private final StatusCode statusCode;
}
