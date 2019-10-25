package com.tanza.kudu;

import com.tanza.kudu.lib.LibConstants.Method;
import com.tanza.kudu.lib.LibConstants.StatusCode;
import com.tanza.kudu.lib.Response;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.ArrayList;
import java.util.Collection;
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

    public static Builder builder() {
        Builder res = new Builder();
        res.handlers = new ArrayList<>();
        return res;
    }

    private RequestDispatcher(Collection<RequestHandler> handlers) {
        this.handlers = handlers.stream().collect(Collectors.toMap(
            h -> new ResourceId(h.getMethod(), h.getPath()), Function.identity())
        );
    }

    Optional<RequestHandler> getHandlerFor(Request request) {
        return request == null
            ? Optional.empty()
            : getHandlerFor(request.getMethod(), request.getUrl().getPath());
    }

    Optional<RequestHandler> getHandlerFor(Method method, String path) {
        ResourceId id = new ResourceId(method, path);
        if (handlers.containsKey(id)) {
            return Optional.ofNullable(handlers.get(id));
        } else {
            return Optional.of(notFound());
        }
    }

    private static RequestHandler notFound() {
        return RequestHandler.defaultHandler(r -> Response.from(StatusCode.NOT_FOUND));
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {
        private List<RequestHandler> handlers;

        public Builder withHandler(RequestHandler handler) {
            this.handlers.add(handler);
            return this;
        }

        public Builder withHandlers(Collection<RequestHandler> handlers) {
            this.handlers.addAll(handlers);
            return this;
        }

        public RequestDispatcher build() {
            return new RequestDispatcher(this.handlers);
        }
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
