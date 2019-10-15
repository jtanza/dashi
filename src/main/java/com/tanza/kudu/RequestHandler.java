package com.tanza.kudu;

import com.tanza.kudu.Constants.Method;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Function;

/**
 *
 * @author jtanza
 */
@Getter
@AllArgsConstructor
public class RequestHandler {
    private static final Method DEFAULT_METHOD = Method.GET;

    private final Method method;
    private final String path;
    private final Function<Request, Response> action;

    public static RequestHandler asGet(String path, Function<Request, Response> action) {
        return new RequestHandler(Method.GET, path, action);
    }

    public static RequestHandler defaultHandler(Function<Request, Response> action) {
        return new RequestHandler(null, null, action);
    }
}
