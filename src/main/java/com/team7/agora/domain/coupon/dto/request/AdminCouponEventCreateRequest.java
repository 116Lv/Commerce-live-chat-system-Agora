package com.team7.agora.domain.coupon.dto.request;

import com.team7.agora.domain.coupon.enums.CouponEventType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record AdminCouponEventCreateRequest(
    @NotNull(message = "쿠폰 이벤트 타입은 필수입니다.")
    CouponEventType type,

    @NotBlank(message = "쿠폰 이벤트 이름은 필수입니다.")
    String name,

    @NotNull(message = "이벤트 시작 시각은 필수입니다.")
    LocalDateTime startAt,

    @NotNull(message = "이벤트 종료 시각은 필수입니다.")
    LocalDateTime endAt,

    @Min(value = 1, message = "발급 수량은 1개 이상이어야 합니다.")
    int totalQuantity,

    @Min(value = 0, message = "할인 금액은 0 이상이어야 합니다.")
    int discountAmount,

    @Min(value = 0, message = "최소 주문 금액은 0 이상이어야 합니다.")
    int minOrderAmount,

    @Min(value = 1, message = "유효 기간은 1일 이상이어야 합니다.")
    int validDays,

    @NotBlank(message = "Coupon event approval reason is required.")
    String reason
) {
}
