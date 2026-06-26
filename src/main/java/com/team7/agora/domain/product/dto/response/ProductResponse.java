package com.team7.agora.domain.product.dto.response;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.enums.ProductStatus;
import java.math.BigDecimal;

/**
 * 응답 본문을 전달하는 DTO이다.
 * @param id 입력 값
 * @param title 입력 값
 * @param description 입력 값
 * @param price 입력 값
 * @param category 입력 값
 * @param status 입력 값
 */
public record ProductResponse(
    Long productId,
    String title,
    String description,
    BigDecimal price,
    String category,
    ProductStatus status
) {

    /**
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param product 입력 값
     * @return 처리 결과
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
