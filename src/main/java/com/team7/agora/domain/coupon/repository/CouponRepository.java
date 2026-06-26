package com.team7.agora.domain.coupon.repository;

import com.team7.agora.domain.coupon.entity.Coupon;
import java.util.List;
import java.util.Optional;

/**
 * Coupon 데이터 저장과 조회를 담당하는 저장소 인터페이스이다.
 */
public interface CouponRepository {

    Coupon save(Coupon coupon);

    Optional<Coupon> findById(Long couponId);

    Optional<Coupon> findFirstComeCoupon();

    List<Coupon> findAll();
}
