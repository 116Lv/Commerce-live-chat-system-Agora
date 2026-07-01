package com.team7.agora.domain.coupon.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;

class CouponCleanupSchedulerTest {

    @Test
    void cleanupSchedulerRunsEveryMidnightUtcByConfiguredCron() throws NoSuchMethodException {
        Method method = CouponCleanupScheduler.class.getDeclaredMethod("cleanupExpiredCouponsAndEvents");
        Scheduled scheduled = method.getAnnotation(Scheduled.class);

        assertThat(scheduled.cron())
            .isEqualTo("${agora.scheduler.coupon-cleanup.cron:0 0 0 * * *}");
        assertThat(scheduled.zone())
            .isEqualTo("${agora.scheduler.coupon-cleanup.zone:UTC}");
        assertThat(scheduled.fixedDelayString()).isEmpty();
        assertThat(scheduled.initialDelayString()).isEmpty();
    }
}
