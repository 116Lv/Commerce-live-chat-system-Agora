package com.team7.agora.global.time;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class AgoraClock {

    private AgoraClock() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
}
