package com.team7.agora.domain.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record ProductCreateRequest(
    @NotBlank(message = "상품 제목은 필수입니다.")
    String title,

    @NotBlank(message = "상품 설명은 필수입니다.")
    String description,

    @NotNull(message = "상품 가격은 필수입니다.")
    @Positive(message = "상품 가격은 0보다 커야 합니다.")
    BigDecimal price,

    @NotBlank(message = "카테고리는 필수입니다.")
    String category,

    @NotNull(message = "지역은 필수입니다.")
    Long regionId
) {
}
