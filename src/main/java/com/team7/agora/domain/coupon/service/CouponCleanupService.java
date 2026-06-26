package com.team7.agora.domain.coupon.service;

import com.team7.agora.domain.coupon.repository.CouponEventRepository;
import com.team7.agora.domain.coupon.repository.CouponRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CouponCleanupService {

    private final CouponEventRepository couponEventRepository;
    private final CouponRepository couponRepository;

    public CouponCleanupService(CouponEventRepository couponEventRepository, CouponRepository couponRepository) {
        this.couponEventRepository = couponEventRepository;
        this.couponRepository = couponRepository;
    }

    @Transactional
    public int expireIssuedCoupons(LocalDateTime now) {
        return couponRepository.expireIssuedCouponsBefore(now);
    }

    @Transactional
    public CleanupResult cleanupExpired(LocalDateTime now) {
        int endedEventCount = couponEventRepository.endActiveEventsBefore(now);
        int deletedAvailableSlotCount = couponRepository.deleteAvailableSlotsForEndedEventsBefore(now);
        int expiredIssuedCouponCount = couponRepository.expireIssuedCouponsBefore(now);
        return new CleanupResult(endedEventCount, deletedAvailableSlotCount, expiredIssuedCouponCount);
    }

    public record CleanupResult(
        int endedEventCount,
        int deletedAvailableSlotCount,
        int expiredIssuedCouponCount
    ) {
    }
}
