// 검색 API 버전별 성능 지표
package com.team7.agora.domain.search.metric;

public record SearchPerformanceStats(
    long callCount,
    double avgResponseTimeMs,
    double tps,
    long dbQueryCount
) {
}
