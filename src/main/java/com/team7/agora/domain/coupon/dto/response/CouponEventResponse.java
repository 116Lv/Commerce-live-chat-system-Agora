package com.team7.agora.domain.coupon.dto.response;

import com.team7.agora.domain.coupon.entity.CouponEvent;
import java.time.LocalDateTime;

/**
 * Response payload for returning coupon event data.
 * @param eventId the event id value
 * @param name the name value
 * @param totalQuantity the total quantity value
 * @param issuedQuantity the issued quantity value
 * @param startAt the start at value
 * @param endAt the end at value
 * @param status the status value
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
     * Creates a response from the given domain object.
     * @param couponEvent the coupon event value
     * @return the from result
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
