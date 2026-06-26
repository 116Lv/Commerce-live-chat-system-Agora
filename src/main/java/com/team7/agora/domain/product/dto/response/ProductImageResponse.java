// 상품 이미지 업로드 응답 DTO
package com.team7.agora.domain.product.dto.response;

import com.team7.agora.domain.product.entity.ProductImage;

/**
 * 상품 이미지 응답 본문을 표현하는 DTO이다.
 * @param imageId 상품 이미지 ID
 * @param productId 상품 ID
 * @param imageUrl 저장된 이미지 접근 URL
 * @param sortOrder 이미지 표시 순서
 */
public record ProductImageResponse(
    Long imageId,
    Long productId,
    String imageUrl,
    int sortOrder
) {

    /**
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param productImage 상품 이미지 엔티티
     * @return 클라이언트에 반환할 API 응답
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
