package com.tanza.kudu;

import com.tanza.kudu.Constants.Method;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author jtanza
 */
public class RequestDispatch {

    private final Map<ResourceId, Handler> handlers;
    private Handler defaultHandler;

    public RequestDispatch() {
        this.handlers = new HashMap<>();
    }

    public RequestDispatch addDefault(Handler defaultHandler) {
        this.defaultHandler = defaultHandler;
        return this;
    }

    public RequestDispatch addHandler(Handler handler) {
        handlers.put(new ResourceId(handler.getMethod(), handler.getPath()), handler);
        return this;
    }

    public Optional<Handler> handlerFor(Method method, String path) {
        ResourceId id = new ResourceId(method, path);
        if (handlers.containsKey(id)) {
            return Optional.ofNullable(handlers.get(id));
        }
        return defaultHandler == null ? Optional.empty() : Optional.of(defaultHandler);
    }

    /**
     * POJO to be used as keys to differentiate
     * requests paths by path + request method
     */
    @Data
    private static class ResourceId {
        private final Method method;
        private final String path;
    }
}
