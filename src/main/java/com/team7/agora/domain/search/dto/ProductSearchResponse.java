package com.team7.agora.domain.search.dto;

import java.math.BigDecimal;

/**
 * 상품 검색 데이터를 전달하는 DTO이다.
 * @param id 식별자
 * @param title 상품 제목 또는 화면에 표시할 제목
 * @param price 가격
 * @param regionName 거래 지역 이름
 */
public record ProductSearchResponse(
    Long id,
    String title,
    BigDecimal price,
    String regionName
) {
}
