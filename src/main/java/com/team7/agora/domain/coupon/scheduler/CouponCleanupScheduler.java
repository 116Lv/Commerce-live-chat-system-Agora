package com.team7.agora.domain.coupon.scheduler;

import com.team7.agora.domain.coupon.service.CouponCleanupService;
import com.team7.agora.global.time.AgoraClock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CouponCleanupScheduler {

    private final CouponCleanupService couponCleanupService;

    public CouponCleanupScheduler(CouponCleanupService couponCleanupService) {
        this.couponCleanupService = couponCleanupService;
    }

    @Scheduled(
        cron = "${agora.scheduler.coupon-cleanup.cron:0 0 0 * * *}",
        zone = "${agora.scheduler.coupon-cleanup.zone:UTC}"
    )
    public void cleanupExpiredCouponsAndEvents() {
        couponCleanupService.cleanupExpired(AgoraClock.now());
    }
}
