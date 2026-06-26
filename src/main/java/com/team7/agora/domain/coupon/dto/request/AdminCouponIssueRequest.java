package com.team7.agora.domain.coupon.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 요청 본문을 전달하는 DTO이다.
 * @param userIds 입력 값
 */
public record AdminCouponIssueRequest(
    @NotEmpty(message = "발급 대상 사용자는 1명 이상이어야 합니다.")
    List<@NotNull(message = "사용자 ID는 비어 있을 수 없습니다.") Long> userIds
) {
}
