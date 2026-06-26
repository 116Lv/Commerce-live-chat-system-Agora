package com.team7.agora.domain.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 요청 본문을 전달하는 DTO이다.
 * @param tradeId 입력 값
 * @param rating 입력 값
 * @param content 입력 값
 */
public record ReviewCreateRequest(
    @NotNull(message = "거래 id는 필수입니다.")
    Long tradeId,

    @Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 5점 이하이어야 합니다.")
    int rating,

    @NotBlank(message = "후기 내용을 입력해 주세요.")
    @Size(max = 500, message = "후기는 500자 이하로 입력해 주세요.")
    String content
) {
}
