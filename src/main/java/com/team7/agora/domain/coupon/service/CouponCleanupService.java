package com.team7.agora.domain.coupon.service;

import com.team7.agora.domain.coupon.repository.CouponRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CouponCleanupService {

    private final CouponRepository couponRepository;

    public CouponCleanupService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Transactional
    public int expireIssuedCoupons(LocalDateTime now) {
        return couponRepository.expireIssuedCouponsBefore(now);
    }
}
