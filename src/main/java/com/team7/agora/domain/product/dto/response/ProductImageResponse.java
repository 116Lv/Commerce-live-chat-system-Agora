// 상품 이미지 업로드 응답 DTO
package com.team7.agora.domain.product.dto.response;

import com.team7.agora.domain.product.entity.ProductImage;

/**
 * 응답 본문을 전달하는 DTO이다.
 * @param imageId 입력 값
 * @param productId 입력 값
 * @param imageUrl 입력 값
 * @param sortOrder 입력 값
 */
public record ProductImageResponse(
    Long imageId,
    Long productId,
    String imageUrl,
    int sortOrder
) {

    /**
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param productImage 입력 값
     * @return 처리 결과
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
