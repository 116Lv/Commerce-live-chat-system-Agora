package com.team7.agora.domain.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for admin report resolve operations.
 * @param adminMemo the admin memo value
 */
public record AdminReportResolveRequest(
    @NotBlank(message = "처리 메모를 입력해 주세요.")
    @Size(max = 1000, message = "처리 메모는 1000자 이하로 입력해 주세요.")
    String adminMemo
) {
}
