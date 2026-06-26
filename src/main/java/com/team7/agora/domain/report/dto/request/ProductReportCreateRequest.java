package com.team7.agora.domain.report.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request payload for product report create operations.
 * @param productId the product id value
 * @param reason the reason value
 */
public record ProductReportCreateRequest(
    @NotNull(message = "신고 대상 상품 ID는 필수입니다.")
    Long productId,

    @NotBlank(message = "신고 사유를 입력해 주세요.")
    @Size(max = 1000, message = "신고 사유는 1000자 이하로 입력해 주세요.")
    String reason
) {
}
