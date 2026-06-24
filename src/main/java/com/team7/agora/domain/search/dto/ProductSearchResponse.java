package com.team7.agora.domain.search.dto;

import java.math.BigDecimal;

public record ProductSearchResponse(
    Long id,
    String title,
    BigDecimal price,
    String regionName
) {
}
