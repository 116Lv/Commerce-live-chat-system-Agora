package com.team7.agora.domain.coupon.dto.request;

import com.team7.agora.domain.coupon.enums.CouponType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Admin Coupon Create 요청 본문을 표현하는 DTO이다.
 * @param name 이름 또는 제목
 * @param discountAmount 쿠폰 할인 금액
 * @param minOrderAmount 쿠폰 사용을 위한 최소 주문 금액
 * @param type 쿠폰 유형
 * @param validDays 쿠폰 유효 일수
 */
public record AdminCouponCreateRequest(
    @NotBlank(message = "쿠폰 이름은 필수입니다.")
    String name,

    @Min(value = 0, message = "할인 금액은 0 이상이어야 합니다.")
    int discountAmount,

    @Min(value = 0, message = "최소 주문 금액은 0 이상이어야 합니다.")
    int minOrderAmount,

    @NotNull(message = "쿠폰 타입은 필수입니다.")
    CouponType type,

    @Min(value = 1, message = "유효 기간은 1일 이상이어야 합니다.")
    int validDays
) {
}
