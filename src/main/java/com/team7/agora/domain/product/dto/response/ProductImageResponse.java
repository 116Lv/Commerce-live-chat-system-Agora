// 상품 이미지 업로드 응답 DTO
package com.team7.agora.domain.product.dto.response;

import com.team7.agora.domain.product.entity.ProductImage;

/**
 * Response payload for returning product image data.
 * @param imageId the image id value
 * @param productId the product id value
 * @param imageUrl the image url value
 * @param sortOrder the sort order value
 */
public record ProductImageResponse(
    Long imageId,
    Long productId,
    String imageUrl,
    int sortOrder
) {

    /**
     * Creates a response from the given domain object.
     * @param productImage the product image value
     * @return the from result
     */
    public static ProductImageResponse from(ProductImage productImage) {
        return new ProductImageResponse(
            productImage.getId(),
            productImage.getProduct().getId(),
            productImage.getImageUrl(),
            productImage.getSortOrder()
        );
    }
}
