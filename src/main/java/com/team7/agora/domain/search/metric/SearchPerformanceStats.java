package com.team7.agora.domain.search.metric;

/**
 * 불변 데이터를 전달하는 객체이다.
 * @param callCount 입력 값
 * @param avgResponseTimeMs 입력 값
 * @param tps 입력 값
 * @param dbQueryCount 입력 값
 */
public record SearchPerformanceStats(
    long callCount,
    double avgResponseTimeMs,
    double tps,
    long dbQueryCount
) {
}
