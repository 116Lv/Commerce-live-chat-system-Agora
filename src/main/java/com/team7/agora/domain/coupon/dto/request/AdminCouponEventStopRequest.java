package com.team7.agora.domain.coupon.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AdminCouponEventStopRequest(
    @NotBlank(message = "Stop reason is required.")
    String reason
) {
}
