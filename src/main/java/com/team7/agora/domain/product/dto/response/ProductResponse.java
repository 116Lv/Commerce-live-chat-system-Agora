package com.team7.agora.domain.product.dto.response;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.enums.ProductStatus;
import java.math.BigDecimal;

public record ProductResponse(
    Long id,
    String title,
    String description,
    BigDecimal price,
    String category,
    ProductStatus status
) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getTitle(),
            product.getDescription(),
            product.getPrice(),
            product.getCategory(),
            product.getStatus()
        );
    }
}
