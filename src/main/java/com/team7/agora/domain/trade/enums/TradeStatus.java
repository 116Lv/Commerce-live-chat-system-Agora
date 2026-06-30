package com.team7.agora.domain.trade.enums;

import java.util.Set;

/**
 * 지원하는 상태 또는 유형 값을 정의한다.
 */
public enum TradeStatus {
    OFFER_ACCEPTED,
    PAYMENT_PENDING,
    PAID,
    COMPLETED,
    EXPIRED,
    CANCELLED;

    public static Set<TradeStatus> blockingStatuses() {
        return Set.of(PAYMENT_PENDING, PAID, COMPLETED);
    }
}
