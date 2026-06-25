package com.team7.agora.domain.search.dto;

import com.team7.agora.domain.search.metric.SearchPerformanceStats;

public record SearchPerformanceResponse(
    SearchPerformanceStats v1,
    SearchPerformanceStats v2
) {

    public static SearchPerformanceResponse of(SearchPerformanceStats v1, SearchPerformanceStats v2) {
        return new SearchPerformanceResponse(v1, v2);
    }
}
