package com.team7.agora.domain.payment.enums;

/**
 * 지원하는 상태 또는 유형 값을 정의한다.
 */
public enum PaymentStatus {
    READY,
    CONFIRMING,
    PAID,
    FAILED,
    CANCELLED,
    REFUNDED
}
