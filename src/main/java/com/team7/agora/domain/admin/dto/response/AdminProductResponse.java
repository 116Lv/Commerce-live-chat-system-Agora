package com.team7.agora.domain.admin.dto.response;

import com.team7.agora.domain.product.entity.Product;
import java.math.BigDecimal;

/**
 * 관리자 상품 응답 본문을 표현하는 DTO이다.
 * @param id 식별자
 * @param title 상품 제목 또는 화면에 표시할 제목
 * @param price 가격
 * @param sellerId 상품 판매자 ID
 * @param status 조회 또는 변경할 상태
 */
public record AdminProductResponse(
        Long id,
        String title,
        BigDecimal price,
        Long sellerId,
        String status
) {

    /**
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param product 상품 엔티티
     * @return 클라이언트에 반환할 API 응답
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
