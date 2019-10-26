package com.tanza.dashi;

import com.tanza.dashi.lib.LibConstants.Method;

import lombok.Value;

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

    public RequestDispatcher() {
        this.handlers = new HashMap<>();
    }

    public RequestDispatcher(Collection<RequestHandler> handlers) {
        this.handlers = handlers.stream().collect(Collectors.toMap(
            h -> new ResourceId(h.getMethod(), h.getPath()), Function.identity())
        );
    }

    /**
     * Adds an {@link RequestHandler} to the underlying {@link RequestDispatcher}.
     * This method is specified to return the dispatcher upon which it was invoked,
     * allowing for chained method invocation.
     *
     * @param handler
     * @return
     */
    public RequestDispatcher addHandler(RequestHandler handler) {
        handlers.put(new ResourceId(handler.getMethod(), handler.getPath()), handler);
        return this;
    }

    Optional<RequestHandler> getHandlerFor(Request request) {
        return request == null
            ? Optional.empty()
            : getHandlerFor(request.getMethod(), request.getUrl().getPath());
    }

    Optional<RequestHandler> getHandlerFor(Method method, String path) {
        ResourceId id = new ResourceId(method, path);
        return handlers.containsKey(id) ? Optional.of(handlers.get(id)) : Optional.empty();
    }

    /**
     * POJO used as keys to differentiate {@link RequestHandler}s by their path + request method,
     * as {@link RequestHandler} do not contain a reference to a {@link Request}
     */
    @Value
    private static class ResourceId {
        private final Method method;
        private final String path;
    }
}
