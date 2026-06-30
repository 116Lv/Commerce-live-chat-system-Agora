package com.team7.agora.domain.coupon.enums;

/**
 * 지원하는 상태 또는 유형 값을 정의한다.
 */
public enum CouponEventStatus {
    PENDING_APPROVAL,
    ACTIVE,
    STOP_REQUESTED,
    STOPPED,
    ENDED,
    REJECTED
}
