package com.team7.agora.domain.search.metric;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/**
 * Component for search performance behavior.
 */
@Component
public class SearchPerformanceRecorder {

    private final ConcurrentHashMap<String, VersionCounters> countersByVersion = new ConcurrentHashMap<>();

    /**
     * Handles record behavior.
     * @param version the version value
     * @param elapsedNanos the elapsed nanos value
     * @param dbQueried the db queried value
     */
    public void record(String version, long elapsedNanos, boolean dbQueried) {
        countersByVersion
            .computeIfAbsent(version, key -> new VersionCounters())
            .record(elapsedNanos, dbQueried);
    }

    /**
     * Returns stats data.
     * @param version the version value
     * @return the get stats result
     */
    public SearchPerformanceStats getStats(String version) {
        VersionCounters counters = countersByVersion.get(version);
        if (counters == null) {
            return new SearchPerformanceStats(0, 0.0, 0.0, 0);
        }
        return counters.toStats();
    }

    private static class VersionCounters {

        private final AtomicLong callCount = new AtomicLong();
        private final AtomicLong totalElapsedNanos = new AtomicLong();
        private final AtomicLong dbQueryCount = new AtomicLong();
        private volatile long firstCallNanos = -1;
        private volatile long lastCallNanos = -1;

        void record(long elapsedNanos, boolean dbQueried) {
            long now = System.nanoTime();
            if (firstCallNanos < 0) {
                firstCallNanos = now;
            }
            lastCallNanos = now;
            callCount.incrementAndGet();
            totalElapsedNanos.addAndGet(elapsedNanos);
            if (dbQueried) {
                dbQueryCount.incrementAndGet();
            }
        }

        SearchPerformanceStats toStats() {
            long calls = callCount.get();
            double avgResponseTimeMs = calls == 0 ? 0.0 : (totalElapsedNanos.get() / 1_000_000.0) / calls;
            double elapsedSeconds = (lastCallNanos - firstCallNanos) / 1_000_000_000.0;
            double tps = calls <= 1 || elapsedSeconds <= 0 ? calls : calls / elapsedSeconds;
            return new SearchPerformanceStats(calls, avgResponseTimeMs, tps, dbQueryCount.get());
        }
    }
}
