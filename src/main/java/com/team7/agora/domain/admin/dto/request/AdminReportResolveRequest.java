package com.team7.agora.domain.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Admin Report Resolve 요청 본문을 표현하는 DTO이다.
 * @param adminMemo 관리자가 신고 처리 시 남기는 메모
 */
public record AdminReportResolveRequest(
    @NotBlank(message = "처리 메모를 입력해 주세요.")
    @Size(max = 1000, message = "처리 메모는 1000자 이하로 입력해 주세요.")
    String adminMemo
) {
}
