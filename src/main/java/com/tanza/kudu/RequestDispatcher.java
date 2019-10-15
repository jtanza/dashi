package com.tanza.kudu;

import com.tanza.kudu.Constants.Method;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author jtanza
 */
public class RequestDispatcher {

    private final Map<ResourceId, RequestHandler> handlers;
    private RequestHandler defaultHandler;

    public RequestDispatcher() {
        this.handlers = new HashMap<>();
    }

    public RequestDispatcher addDefault(RequestHandler defaultHandler) {
        this.defaultHandler = defaultHandler;
        return this;
    }

    public RequestDispatcher addHandler(RequestHandler handler) {
        handlers.put(new ResourceId(handler.getMethod(), handler.getPath()), handler);
        return this;
    }

    public Optional<RequestHandler> getHandlerFor(Request request) {
        return getHandlerFor(request.getMethod(), request.getUrl().getPath());
    }

    Optional<RequestHandler> getHandlerFor(Method method, String path) {
        ResourceId id = new ResourceId(method, path);
        if (handlers.containsKey(id)) {
            return Optional.ofNullable(handlers.get(id));
        }
        return defaultHandler == null ? Optional.empty() : Optional.of(defaultHandler);
    }

    /**
     * POJO to be used as keys to differentiate
     * requests paths by path + request method
     *
     * //TODO get rid of this, impl equals in Request
     */
    @Data
    private static class ResourceId {
        private final Method method;
        private final String path;
    }
}
