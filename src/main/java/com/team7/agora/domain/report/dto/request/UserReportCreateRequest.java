package com.team7.agora.domain.report.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request payload for user report create operations.
 * @param reportedUserId the reported user id value
 * @param reason the reason value
 */
public record UserReportCreateRequest(
    @NotNull(message = "신고 대상 회원 ID는 필수입니다.")
    Long reportedUserId,

    @NotBlank(message = "신고 사유를 입력해 주세요.")
    @Size(max = 1000, message = "신고 사유는 1000자 이하로 입력해 주세요.")
    String reason
) {
}
