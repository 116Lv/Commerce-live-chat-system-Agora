package com.team7.agora.domain.search.dto;

import com.team7.agora.domain.search.metric.SearchPerformanceStats;

/**
 * 데이터 전송에 사용하는 DTO이다.
 * @param v1 입력 값
 * @param v2 입력 값
 */
public record SearchPerformanceResponse(
    SearchPerformanceStats v1,
    SearchPerformanceStats v2
) {

    /**
     * 요청한 동작을 처리한다.
     * @param v1 입력 값
     * @param v2 입력 값
     * @return 처리 결과
     */
    public static SearchPerformanceResponse of(SearchPerformanceStats v1, SearchPerformanceStats v2) {
        return new SearchPerformanceResponse(v1, v2);
    }
}
