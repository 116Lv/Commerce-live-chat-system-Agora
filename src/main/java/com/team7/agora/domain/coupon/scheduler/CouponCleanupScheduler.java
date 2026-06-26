package com.team7.agora.domain.coupon.scheduler;

import com.team7.agora.domain.coupon.service.CouponCleanupService;
import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CouponCleanupScheduler {

    private final CouponCleanupService couponCleanupService;

    public CouponCleanupScheduler(CouponCleanupService couponCleanupService) {
        this.couponCleanupService = couponCleanupService;
    }

    @Scheduled(
        fixedDelayString = "${agora.scheduler.coupon-cleanup.fixed-delay:300000}",
        initialDelayString = "${agora.scheduler.coupon-cleanup.initial-delay:300000}"
    )
    public void cleanupExpiredCouponsAndEvents() {
        couponCleanupService.cleanupExpired(LocalDateTime.now());
    }
}
