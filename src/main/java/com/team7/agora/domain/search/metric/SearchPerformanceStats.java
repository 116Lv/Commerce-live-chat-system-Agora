package com.team7.agora.domain.search.metric;

/**
 * 불변 데이터를 전달하는 객체이다.
 * @param callCount 검색 API 호출 횟수
 * @param avgResponseTimeMs 평균 응답 시간 밀리초
 * @param tps 초당 처리 요청 수
 * @param dbQueryCount DB 조회 발생 횟수
 */
public record SearchPerformanceStats(
    long callCount,
    double avgResponseTimeMs,
    double tps,
    long dbQueryCount
) {
}
