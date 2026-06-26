package com.team7.agora.domain.search.dto;

import com.team7.agora.domain.search.metric.SearchPerformanceStats;

/**
 * 검색 성능 데이터를 전달하는 DTO이다.
 * @param v1 기존 검색 로직 성능 지표
 * @param v2 개선된 검색 로직 성능 지표
 */
public record SearchPerformanceResponse(
    SearchPerformanceStats v1,
    SearchPerformanceStats v2
) {

    /**
     * 도메인 객체를 클라이언트 응답 DTO로 변환한다.
     * @param v1 기존 검색 로직 성능 지표
     * @param v2 개선된 검색 로직 성능 지표
     * @return 클라이언트에 반환할 API 응답
     */
    public static SearchPerformanceResponse of(SearchPerformanceStats v1, SearchPerformanceStats v2) {
        return new SearchPerformanceResponse(v1, v2);
    }
}
