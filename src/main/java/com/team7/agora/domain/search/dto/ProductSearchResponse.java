package com.team7.agora.domain.search.dto;

import java.math.BigDecimal;

/**
 * Data transfer object for product search data.
 * @param id the id value
 * @param title the title value
 * @param price the price value
 * @param regionName the region name value
 */
public record ProductSearchResponse(
    Long id,
    String title,
    BigDecimal price,
    String regionName
) {
}
