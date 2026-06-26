package com.team7.agora.domain.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Product Create 요청 본문을 표현하는 DTO이다.
 * @param title 상품 제목 또는 화면에 표시할 제목
 * @param description 상품 설명 또는 상세 내용
 * @param price 가격
 * @param category 업로드 카테고리
 * @param regionId 지역 ID
 */
public record ProductCreateRequest(
    @NotBlank(message = "상품 제목은 필수입니다.")
    String title,

    @NotBlank(message = "상품 설명은 필수입니다.")
    String description,

    @NotNull(message = "상품 가격은 필수입니다.")
    @Positive(message = "상품 가격은 0보다 커야 합니다.")
    BigDecimal price,

    @NotBlank(message = "카테고리는 필수입니다.")
    String category,

    @NotNull(message = "지역은 필수입니다.")
    Long regionId
) {
}
