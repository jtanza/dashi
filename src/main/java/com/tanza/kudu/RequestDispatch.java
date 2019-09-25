package com.tanza.kudu;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author jtanza
 */
public class RequestDispatch {

    private final Map<String, Function<Request, Response>> handlers;
    private Function<Request, Response> defaultHandler;

    public RequestDispatch() {
        this.handlers = new HashMap<>();
    }

    public RequestDispatch addDefault(Function<Request, Response> defaultHandler) {
        this.defaultHandler = defaultHandler;
        return this;
    }

    public RequestDispatch addHandler(String path, Function<Request, Response> handler) {
        handlers.put(path, handler);
        return this;
    }

    public Function<Request, Response> handlerFor(String path) {
        if (handlers.containsKey(path)) {
            return handlers.get(path);
        }
        return Objects.isNull(defaultHandler) ? null : defaultHandler;
    }
}
