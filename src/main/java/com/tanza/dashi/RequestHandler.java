package com.tanza.dashi;

import com.tanza.dashi.lib.LibConstants;
import com.tanza.dashi.lib.Response;
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
    private static final LibConstants.Method DEFAULT_METHOD = LibConstants.Method.GET;

    private final LibConstants.Method method;
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
