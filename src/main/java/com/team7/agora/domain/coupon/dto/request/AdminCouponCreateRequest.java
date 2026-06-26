package com.team7.agora.domain.coupon.dto.request;

import com.team7.agora.domain.coupon.enums.CouponType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 요청 본문을 전달하는 DTO이다.
 * @param name 입력 값
 * @param discountAmount 입력 값
 * @param minOrderAmount 입력 값
 * @param type 입력 값
 * @param validDays 입력 값
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
