package com.team7.agora.domain.search.dto;

/**
 * 인기 검색어 데이터를 전달하는 DTO이다.
 * @param keyword 검색어
 * @param count 조회 또는 집계된 개수
 */
public record PopularKeywordResponse(
    String keyword,
    long count
) {
}
