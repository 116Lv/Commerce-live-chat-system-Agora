package com.team7.agora.domain.admin.dto.response;

import com.team7.agora.domain.product.entity.Product;
import java.math.BigDecimal;

/**
 * Response payload for returning admin product data.
 * @param id the id value
 * @param title the title value
 * @param price the price value
 * @param sellerId the seller id value
 * @param status the status value
 */
public record AdminProductResponse(
        Long id,
        String title,
        BigDecimal price,
        Long sellerId,
        String status
) {

    /**
     * Creates a response from the given domain object.
     * @param product the product value
     * @return the from result
     */
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
