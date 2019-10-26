package com.tanza.dashi;

import com.tanza.dashi.lib.LibConstants.Method;

import lombok.Value;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author jtanza
 */
public class RequestDispatcher {

    private final Map<ResourceId, RequestHandler> handlers;
    private final List<RequestHandler> variableHandlers;

    public RequestDispatcher() {
        this.handlers = new HashMap<>();
        this.variableHandlers = new ArrayList<>();
    }

    public RequestDispatcher(Collection<RequestHandler> handlers) {
        this.handlers = handlers.stream().collect(Collectors.toMap(
            h -> new ResourceId(h.getMethod(), h.getPath()), Function.identity())
        );
        this.variableHandlers = new ArrayList<>();
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
        String path = handler.getPath();
        handlers.put(new ResourceId(handler.getMethod(), path), handler);
        if (isVarPath(path)) {
            variableHandlers.add(handler);
        }
        return this;
    }

    Optional<RequestHandler> getHandlerFor(Request request) {
        return request == null
            ? Optional.empty()
            : getHandlerFor(request.getMethod(), request.getUrl().getPath());
    }

    Optional<RequestHandler> getHandlerFor(Method requestMethod, String requestPath) {
        ResourceId id = new ResourceId(requestMethod, requestPath);
        return handlers.containsKey(id)
            ? Optional.of(handlers.get(id))
            : Optional.ofNullable(findVariableHandler(requestPath));
    }

    private RequestHandler findVariableHandler(String requestPath) {
        RequestHandler ret = null;
        String[] requestPathSegments = requestPath.split("/");

        int maxIndexDiff = Integer.MIN_VALUE;
        for (RequestHandler handler : variableHandlers) {
            String varPath = handler.getPath();
            if (isCongruentPaths(varPath.split("/"), requestPathSegments)) {
                int index = StringUtils.indexOfDifference(varPath, requestPath);
                if (index > maxIndexDiff) {
                    maxIndexDiff = index;
                    ret = handler;
                }
            }
        }
        return ret;
    }

    private static boolean isCongruentPaths(String[] varPath, String[] requestPath) {
        for (int i = 0; i < varPath.length; i++) {
            String variablePathSegment = varPath[i];
            if (!isVarPath(variablePathSegment) && !variablePathSegment.equals(requestPath[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean isVarPath(String path) {
        return path.indexOf('{') >= 0;
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
