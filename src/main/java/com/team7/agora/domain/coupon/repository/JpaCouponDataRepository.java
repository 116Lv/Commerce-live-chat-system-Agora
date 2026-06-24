package com.team7.agora.domain.coupon.repository;

import com.team7.agora.domain.coupon.entity.Coupon;
import com.team7.agora.domain.coupon.enums.CouponStatus;
import com.team7.agora.domain.coupon.enums.CouponType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface JpaCouponDataRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findFirstByTypeAndStatus(CouponType type, CouponStatus status);
}
