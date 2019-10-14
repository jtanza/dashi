package com.tanza.kudu;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.AllArgsConstructor;

import java.nio.channels.SelectionKey;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author jtanza
 */
@AllArgsConstructor
public class RequestProcessor {
    private static final long CACHE_TTL = 10;

    private final Cache<SelectionKey, Response> cache = CacheBuilder.newBuilder()
        .expireAfterWrite(CACHE_TTL, TimeUnit.MINUTES)
        .build();

    public void processAsync(SelectionKey key, Request request, Handler handler) {
        CompletableFuture
            .supplyAsync(() -> handler.getAction().apply(request))
            .thenAccept(response -> cache.put(key, response));
        //TODO thenAccept publishEvent
    }

    public Optional<Response> getResponse(SelectionKey key) {
        Response response = cache.getIfPresent(key);
        if (response != null) {
            cache.invalidate(key);
        }
        return Optional.ofNullable(response);
    }
}
