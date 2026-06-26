package com.team7.agora.domain.report.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 요청 본문을 전달하는 DTO이다.
 * @param productId 입력 값
 * @param reason 입력 값
 */
public record ProductReportCreateRequest(
    @NotNull(message = "신고 대상 상품 ID는 필수입니다.")
    Long productId,

    @NotBlank(message = "신고 사유를 입력해 주세요.")
    @Size(max = 1000, message = "신고 사유는 1000자 이하로 입력해 주세요.")
    String reason
) {
}
