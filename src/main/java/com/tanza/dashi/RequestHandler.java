package com.tanza.dashi;

import com.tanza.dashi.HttpConstants.Method;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Function;

/**
 * {@link RequestHandler}s encapsulate the user defined logic
 * of how to respond to an incoming client {@link Request} matching
 * a specified {@link #method} and {@link #path}.
 *
 * A {@link #path} can be both absolute or variable depending on its syntax.
 * To construct a variable segment path, a user can provide a resource path
 * with variable segments surrounded by <code>{}</code> brackets. For example,
 * given an {@link #path} of <code>/users/{userId}/orders/{orderId}</code>, this
 * {@link RequestHandler} will match any request with variable values in
 * <code>{userId}</code> or <code>{orderId}</code>. The value of these variable
 * identifiers will be available for use via {@link Request#getPathVariable(String)}.
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
}
