package com.team7.agora.domain.coupon.dto.request;

import com.team7.agora.domain.coupon.enums.CouponType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for admin coupon create operations.
 * @param name the name value
 * @param discountAmount the discount amount value
 * @param minOrderAmount the min order amount value
 * @param type the type value
 * @param validDays the valid days value
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
