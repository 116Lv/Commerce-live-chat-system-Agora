// 상품 찜 등록/취소 응답 DTO
package com.team7.agora.domain.product.dto.response;

import com.team7.agora.domain.product.entity.Product;

/**
 * 상품 좋아요 응답 본문을 표현하는 DTO이다.
 * @param productId 상품 ID
 * @param liked 현재 사용자의 좋아요 여부
 * @param likeCount 상품 좋아요 수
 */
public record ProductLikeResponse(
    Long productId,
    boolean liked,
    int likeCount
) {

    /**
     * 도메인 객체를 클라이언트 응답 DTO로 변환한다.
     * @param product 상품 엔티티
     * @param liked 현재 사용자의 좋아요 여부
     * @return 클라이언트에 반환할 API 응답
     */
    public static ProductLikeResponse of(Product product, boolean liked) {
        return new ProductLikeResponse(product.getId(), liked, product.getLikeCount());
    }
}
