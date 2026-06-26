package com.team7.agora.domain.search.dto;

import com.team7.agora.domain.search.metric.SearchPerformanceStats;

/**
 * Data transfer object for search performance data.
 * @param v1 the v1 value
 * @param v2 the v2 value
 */
public record SearchPerformanceResponse(
    SearchPerformanceStats v1,
    SearchPerformanceStats v2
) {

    /**
     * Handles of behavior.
     * @param v1 the v1 value
     * @param v2 the v2 value
     * @return the of result
     */
    public static SearchPerformanceResponse of(SearchPerformanceStats v1, SearchPerformanceStats v2) {
        return new SearchPerformanceResponse(v1, v2);
    }
}
