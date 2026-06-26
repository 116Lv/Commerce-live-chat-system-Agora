package com.team7.agora.domain.coupon.repository;

import com.team7.agora.domain.coupon.entity.Coupon;
import java.util.List;
import java.util.Optional;

/**
 * Repository contract for storing and querying coupon data.
 */
public interface CouponRepository {

    Coupon save(Coupon coupon);

    Optional<Coupon> findById(Long couponId);

    Optional<Coupon> findFirstComeCoupon();
}
