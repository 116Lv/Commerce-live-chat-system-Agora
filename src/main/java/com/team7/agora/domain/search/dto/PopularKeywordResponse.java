package com.team7.agora.domain.search.dto;

public record PopularKeywordResponse(
    String keyword,
    long count
) {
}
