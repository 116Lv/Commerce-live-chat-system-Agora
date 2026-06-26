package com.team7.agora.domain.coupon.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AdminCouponEventIssueRequest(
    @NotEmpty(message = "발급 대상 사용자는 1명 이상이어야 합니다.")
    @Size(max = 100, message = "관리자 개별 발급은 한 번에 100명까지만 가능합니다.")
    List<@NotNull(message = "사용자 ID는 비어 있을 수 없습니다.") Long> userIds
) {
}
