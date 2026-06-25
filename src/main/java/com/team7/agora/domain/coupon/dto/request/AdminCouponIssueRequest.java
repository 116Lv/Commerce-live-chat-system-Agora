package com.team7.agora.domain.coupon.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AdminCouponIssueRequest(
    @NotEmpty(message = "발급 대상 사용자는 1명 이상이어야 합니다.")
    List<Long> userIds
) {
}
