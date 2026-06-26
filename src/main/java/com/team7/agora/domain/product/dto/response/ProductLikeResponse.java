// 상품 찜 등록/취소 응답 DTO
package com.team7.agora.domain.product.dto.response;

import com.team7.agora.domain.product.entity.Product;

/**
 * Response payload for returning product like data.
 * @param productId the product id value
 * @param liked the liked value
 * @param likeCount the like count value
 */
public record ProductLikeResponse(
    Long productId,
    boolean liked,
    int likeCount
) {

    /**
     * Handles of behavior.
     * @param product the product value
     * @param liked the liked value
     * @return the of result
     */
    public static ProductLikeResponse of(Product product, boolean liked) {
        return new ProductLikeResponse(product.getId(), liked, product.getLikeCount());
    }
}
