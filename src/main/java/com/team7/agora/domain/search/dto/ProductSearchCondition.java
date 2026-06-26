package com.team7.agora.domain.search.dto;

import org.springframework.data.domain.Pageable;

/**
 * Data transfer object for product search condition data.
 * @param keyword the keyword value
 * @param regionId the region id value
 * @param category the category value
 * @param pageable the pageable value
 */
public record ProductSearchCondition(
    String keyword,
    Long regionId,
    String category,
    Pageable pageable
) {

    /**
     * Handles normalized keyword behavior.
     * @return the normalized keyword result
     */
    public String normalizedKeyword() {
        if (keyword == null) {
            return "";
        }
        return keyword.trim().toLowerCase();
    }
}
