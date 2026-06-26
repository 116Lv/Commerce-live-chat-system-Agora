package com.team7.agora.domain.search.dto;

/**
 * Data transfer object for popular keyword data.
 * @param keyword the keyword value
 * @param count the count value
 */
public record PopularKeywordResponse(
    String keyword,
    long count
) {
}
