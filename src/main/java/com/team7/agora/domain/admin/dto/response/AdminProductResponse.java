package com.team7.agora.domain.admin.dto.response;

import com.team7.agora.domain.product.entity.Product;
import java.math.BigDecimal;

/**
 * 응답 본문을 전달하는 DTO이다.
 * @param id 입력 값
 * @param title 입력 값
 * @param price 입력 값
 * @param sellerId 입력 값
 * @param status 입력 값
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
     * @param product 입력 값
     * @return 처리 결과
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
