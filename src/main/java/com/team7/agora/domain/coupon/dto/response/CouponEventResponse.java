package com.team7.agora.domain.coupon.dto.response;

import com.team7.agora.domain.coupon.entity.CouponEvent;
import java.time.LocalDateTime;

/**
 * 응답 본문을 전달하는 DTO이다.
 * @param eventId 입력 값
 * @param name 입력 값
 * @param totalQuantity 입력 값
 * @param issuedQuantity 입력 값
 * @param startAt 입력 값
 * @param endAt 입력 값
 * @param status 입력 값
 */
public record CouponEventResponse(
    Long eventId,
    String name,
    int totalQuantity,
    int issuedQuantity,
    LocalDateTime startAt,
    LocalDateTime endAt,
    String status
) {

    /**
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param couponEvent 입력 값
     * @return 처리 결과
     */
    public static CouponEventResponse from(CouponEvent couponEvent) {
        return new CouponEventResponse(
            couponEvent.getId(),
            couponEvent.getName(),
            couponEvent.getTotalQuantity(),
            couponEvent.getIssuedQuantity(),
            couponEvent.getStartAt(),
            couponEvent.getEndAt(),
            couponEvent.getStatus().name()
        );
    }
}
