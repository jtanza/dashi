package com.tanza.kudu;

import com.tanza.kudu.Constants.Method;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Function;

/**
 * @author jtanza
 */
@Getter
@AllArgsConstructor
public class Handler {
    private static final Method DEFAULT_METHOD = Method.GET;

    private final Method method;
    private final String path;
    private final Function<Request, Response> action;

    public static Handler asGet(String path, Function<Request, Response> action) {
        return new Handler(Method.GET, path, action);
    }

    public static Handler defaultHandler(Function<Request, Response> action) {
        return new Handler(null, null, action);
    }
}
