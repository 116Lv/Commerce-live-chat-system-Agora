package com.team7.agora.domain.coupon.dto.response;

import com.team7.agora.domain.coupon.entity.CouponEvent;
import java.time.LocalDateTime;

/**
 * 쿠폰 이벤트 응답 본문을 표현하는 DTO이다.
 * @param eventId 이벤트 ID
 * @param name 이름 또는 제목
 * @param totalQuantity 이벤트 전체 발급 수량
 * @param issuedQuantity 이미 발급된 쿠폰 수량
 * @param startAt 이벤트 시작 시각
 * @param endAt 이벤트 종료 시각
 * @param status 조회 또는 변경할 상태
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
     * @param couponEvent 쿠폰 이벤트 엔티티
     * @return 클라이언트에 반환할 API 응답
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
