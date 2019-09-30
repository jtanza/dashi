package com.tanza.kudu;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Function;

/**
 * @author jtanza
 */
@Getter
@AllArgsConstructor
public class Handler {
    private final String path;
    private final Function<Request, Response> action;

    public static Handler defaultHandler(Function<Request, Response> action) {
        return new Handler(null, action);
    }
}
