package com.team7.agora.domain.search.dto;

import org.springframework.data.domain.Pageable;

public record ProductSearchCondition(
    String keyword,
    Long regionId,
    String category,
    Pageable pageable
) {

    public String normalizedKeyword() {
        if (keyword == null) {
            return "";
        }
        return keyword.trim().toLowerCase();
    }
}
