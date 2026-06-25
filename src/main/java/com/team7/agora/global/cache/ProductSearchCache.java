package com.team7.agora.global.cache;

import com.team7.agora.domain.search.dto.ProductSearchCondition;
import com.team7.agora.domain.search.dto.ProductSearchResponse;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ProductSearchCache {

    private final int maximumSize;
    private final long ttlMillis;
    private final Clock clock;
    private final Map<String, CacheEntry> entries;

    public ProductSearchCache() {
        this(1_000, 60_000L);
    }

    public ProductSearchCache(int maximumSize, long ttlMillis) {
        this(maximumSize, ttlMillis, Clock.systemUTC());
    }

    ProductSearchCache(int maximumSize, long ttlMillis, Clock clock) {
        this.maximumSize = maximumSize;
        this.ttlMillis = ttlMillis;
        this.clock = clock;
        this.entries = new LinkedHashMap<>(16, 0.75f, true);
    }

    public synchronized Optional<List<ProductSearchResponse>> get(ProductSearchCondition condition) {
        String key = keyOf(condition);
        CacheEntry entry = entries.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.expired(clock.millis(), ttlMillis)) {
            entries.remove(key);
            return Optional.empty();
        }
        return Optional.of(entry.responses());
    }

    public synchronized void put(ProductSearchCondition condition, List<ProductSearchResponse> responses) {
        entries.put(keyOf(condition), new CacheEntry(List.copyOf(responses), clock.millis()));
        evictOverflow();
    }

    public synchronized void clear() {
        entries.clear();
    }

    private String keyOf(ProductSearchCondition condition) {
        return String.join(":",
            condition.normalizedKeyword(),
            String.valueOf(condition.regionId()),
            String.valueOf(condition.category()),
            String.valueOf(condition.pageable().getPageNumber()),
            String.valueOf(condition.pageable().getPageSize())
        );
    }

    private void evictOverflow() {
        while (entries.size() > maximumSize) {
            String firstKey = entries.keySet().iterator().next();
            entries.remove(firstKey);
        }
    }

    private record CacheEntry(
        List<ProductSearchResponse> responses,
        long createdAt
    ) {

        boolean expired(long now, long ttlMillis) {
            return now - createdAt > ttlMillis;
        }
    }
}
