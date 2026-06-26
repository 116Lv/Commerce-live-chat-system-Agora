package com.team7.agora.domain.coupon.repository;

import com.team7.agora.domain.coupon.entity.Coupon;
import java.util.List;
import java.util.Optional;

/**
 * 데이터 저장과 조회를 위한 저장소 계약이다.
 */
public interface CouponRepository {

    Coupon save(Coupon coupon);

    Optional<Coupon> findById(Long couponId);

    Optional<Coupon> findFirstComeCoupon();

    List<Coupon> findAll();
}
