package com.tanza.kudu;

import com.tanza.kudu.Constants.Method;
import lombok.Data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author jtanza
 */
public class RequestDispatcher {

    private final Map<ResourceId, RequestHandler> handlers;
    private RequestHandler defaultHandler;

    public RequestDispatcher() {
        this.handlers = new HashMap<>();
    }

    public RequestDispatcher(Collection<RequestHandler> handlers) {
        this.handlers = handlers.stream()
            .collect(Collectors.toMap(
                h -> new ResourceId(h.getMethod(), h.getPath()), Function.identity())
            );
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
        return request == null
            ? Optional.empty()
            : getHandlerFor(request.getMethod(), request.getUrl().getPath());
    }

    Optional<RequestHandler> getHandlerFor(Method method, String path) {
        ResourceId id = new ResourceId(method, path);
        if (handlers.containsKey(id)) {
            return Optional.ofNullable(handlers.get(id));
        } else {
            return Optional.ofNullable(defaultHandler);
        }
    }

    /**
     * POJO used as keys to differentiate {@link RequestHandler}s by their path + request method,
     * as {@link RequestHandler} do not contain a reference to a {@link Request}
     */
    @Data
    private static class ResourceId {
        private final Method method;
        private final String path;
    }
}
