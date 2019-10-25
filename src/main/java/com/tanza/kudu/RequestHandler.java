package com.tanza.kudu;

import com.tanza.kudu.lib.LibConstants.Method;
import com.tanza.kudu.lib.Response;
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

    public RequestHandler(String path, Function<Request, Response> action) {
        this.method = DEFAULT_METHOD;
        this.path = path;
        this.action = action;
    }

    public static RequestHandler defaultHandler(Function<Request, Response> action) {
        return new RequestHandler(null, null, action);
    }
}
