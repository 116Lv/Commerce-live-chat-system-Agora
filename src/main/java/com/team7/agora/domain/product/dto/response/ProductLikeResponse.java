// 상품 찜 등록/취소 응답 DTO
package com.team7.agora.domain.product.dto.response;

import com.team7.agora.domain.product.entity.Product;

/**
 * 응답 본문을 전달하는 DTO이다.
 * @param productId 입력 값
 * @param liked 입력 값
 * @param likeCount 입력 값
 */
public record ProductLikeResponse(
    Long productId,
    boolean liked,
    int likeCount
) {

    /**
     * 요청한 동작을 처리한다.
     * @param product 입력 값
     * @param liked 입력 값
     * @return 처리 결과
     */
    public static ProductLikeResponse of(Product product, boolean liked) {
        return new ProductLikeResponse(product.getId(), liked, product.getLikeCount());
    }
}
