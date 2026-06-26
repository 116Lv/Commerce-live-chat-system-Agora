package com.team7.agora.domain.search.dto;

/**
 * 데이터 전송에 사용하는 DTO이다.
 * @param keyword 입력 값
 * @param count 입력 값
 */
public record PopularKeywordResponse(
    String keyword,
    long count
) {
}
