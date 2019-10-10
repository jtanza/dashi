package com.tanza.kudu;

import lombok.AllArgsConstructor;

import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author jtanza
 */
@AllArgsConstructor
public class RequestProcessor {

    // FIXME use a cache w/ a TTL
    private final Map<SelectionKey, Response> map = new HashMap<>();

    public void processAsync(SelectionKey key, Request request, Handler handler) {
        CompletableFuture
            .supplyAsync(() -> handler.getAction().apply(request))
            .thenApply(response -> map.put(key, response));
    }

    public Optional<Response> getResponse(SelectionKey key) {
        if (map.containsKey(key)) {
            Response response = map.get(key);
            map.remove(key);
            return Optional.of(response);
        }
        return Optional.empty();
    }
}
