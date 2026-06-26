package com.team7.agora.domain.product.dto.response;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.enums.ProductStatus;
import java.math.BigDecimal;

/**
 * Response payload for returning product data.
 * @param id the id value
 * @param title the title value
 * @param description the description value
 * @param price the price value
 * @param category the category value
 * @param status the status value
 */
public record ProductResponse(
    Long id,
    String title,
    String description,
    BigDecimal price,
    String category,
    ProductStatus status
) {

    /**
     * Creates a response from the given domain object.
     * @param product the product value
     * @return the from result
     */
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
