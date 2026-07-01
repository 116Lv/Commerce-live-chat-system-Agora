package com.team7.agora.domain.coupon.time;

import java.time.LocalDateTime;
import java.time.ZoneId;

public final class CouponEventTime {

    private static final ZoneId OPERATION_ZONE = ZoneId.of("Asia/Seoul");

    private CouponEventTime() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(OPERATION_ZONE);
    }
}
