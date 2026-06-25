package com.team7.agora.domain.search.metric;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SearchPerformanceRecorderTest {

    @Test
    void getStatsReturnsZeroStatsForUnknownVersion() {
        SearchPerformanceRecorder recorder = new SearchPerformanceRecorder();

        SearchPerformanceStats stats = recorder.getStats("v1");

        assertThat(stats.callCount()).isZero();
        assertThat(stats.dbQueryCount()).isZero();
    }

    @Test
    void recordAccumulatesCallCountAndDbQueryCount() {
        SearchPerformanceRecorder recorder = new SearchPerformanceRecorder();

        recorder.record("v1", 1_000_000L, true);
        recorder.record("v1", 2_000_000L, true);

        SearchPerformanceStats stats = recorder.getStats("v1");

        assertThat(stats.callCount()).isEqualTo(2);
        assertThat(stats.dbQueryCount()).isEqualTo(2);
        assertThat(stats.avgResponseTimeMs()).isEqualTo(1.5);
    }

    @Test
    void recordTracksDbQueryCountSeparatelyFromCallCount() {
        SearchPerformanceRecorder recorder = new SearchPerformanceRecorder();

        recorder.record("v2", 1_000_000L, true);
        recorder.record("v2", 500_000L, false);

        SearchPerformanceStats stats = recorder.getStats("v2");

        assertThat(stats.callCount()).isEqualTo(2);
        assertThat(stats.dbQueryCount()).isEqualTo(1);
    }
}
