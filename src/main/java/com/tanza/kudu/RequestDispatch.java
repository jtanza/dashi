package com.tanza.kudu;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author jtanza
 */
public class RequestDispatch {

    private final Map<String, Handler> handlers;
    private Handler defaultHandler;

    public RequestDispatch() {
        this.handlers = new HashMap<>();
    }

    public RequestDispatch addDefault(Handler defaultHandler) {
        this.defaultHandler = defaultHandler;
        return this;
    }

    public RequestDispatch addHandler(Handler handler) {
        handlers.put(handler.getPath(), handler);
        return this;
    }

    public Optional<Handler> handlerFor(String path) {
        if (handlers.containsKey(path)) {
            return Optional.ofNullable(handlers.get(path));
        }
        return defaultHandler == null ? Optional.empty() : Optional.of(defaultHandler);
    }
}
