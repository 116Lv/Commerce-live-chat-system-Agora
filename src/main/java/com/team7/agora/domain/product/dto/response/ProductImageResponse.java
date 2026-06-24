// 상품 이미지 업로드 응답 DTO
package com.team7.agora.domain.product.dto.response;

import com.team7.agora.domain.product.entity.ProductImage;

public record ProductImageResponse(
        Long imageId,
        Long productId,
        String imageUrl,
        int sortOrder
) {

    public static ProductImageResponse from(ProductImage productImage) {
        return new ProductImageResponse(
                productImage.getId(),
                productImage.getProduct().getId(),
                productImage.getImageUrl(),
                productImage.getSortOrder()
        );
    }
}
