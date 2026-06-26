package com.team7.agora.domain.search.dto;

import java.math.BigDecimal;

/**
 * 데이터 전송에 사용하는 DTO이다.
 * @param id 입력 값
 * @param title 입력 값
 * @param price 입력 값
 * @param regionName 입력 값
 */
public record ProductSearchResponse(
    Long id,
    String title,
    BigDecimal price,
    String regionName
) {
}
