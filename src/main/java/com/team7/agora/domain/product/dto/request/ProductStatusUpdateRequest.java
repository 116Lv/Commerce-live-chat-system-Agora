package com.team7.agora.domain.product.dto.request;

import com.team7.agora.domain.product.enums.ProductStatus;
import jakarta.validation.constraints.NotNull;

public record ProductStatusUpdateRequest(
    @NotNull(message = "상품 상태는 필수입니다.")
    ProductStatus status
) {
}
