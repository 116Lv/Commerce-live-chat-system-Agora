package com.team7.agora.domain.search.metric;

/**
 * Immutable data carrier for search performance stats data.
 * @param callCount the call count value
 * @param avgResponseTimeMs the avg response time ms value
 * @param tps the tps value
 * @param dbQueryCount the db query count value
 */
public record SearchPerformanceStats(
    long callCount,
    double avgResponseTimeMs,
    double tps,
    long dbQueryCount
) {
}
