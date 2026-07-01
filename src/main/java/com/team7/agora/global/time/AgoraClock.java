package com.team7.agora.global.time;

import java.time.LocalDateTime;
import java.time.ZoneId;

public final class AgoraClock {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    private AgoraClock() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(SERVICE_ZONE);
    }
}
