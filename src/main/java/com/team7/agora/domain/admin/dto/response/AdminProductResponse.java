package com.team7.agora.domain.admin.dto.response;

import com.team7.agora.domain.product.entity.Product;
import java.math.BigDecimal;

public record AdminProductResponse(
        Long id,
        String title,
        BigDecimal price,
        Long sellerId,
        String status
) {

    public static AdminProductResponse from(Product product) {
        return new AdminProductResponse(
                product.getId(),
                product.getTitle(),
                product.getPrice(),
                product.getSeller().getId(),
                product.getStatus().name()
        );
    }
}
