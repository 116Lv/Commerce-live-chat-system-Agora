package com.team7.agora.domain.coupon.repository;

import com.team7.agora.domain.coupon.entity.Coupon;
import com.team7.agora.domain.coupon.enums.CouponStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CouponRepository {

    Coupon save(Coupon coupon);

    List<Coupon> saveAll(List<Coupon> coupons);

    Optional<Coupon> findById(Long couponId);

    Optional<Coupon> findFirstAvailableSlotForUpdate(Long eventId);

    List<Coupon> findAllByUserIdAndStatusIn(Long userId, Collection<CouponStatus> statuses);

    List<Coupon> findAllByCouponEventIdAndUserIsNotNull(Long eventId);

    boolean existsByCouponEventIdAndUserId(Long eventId, Long userId);

    void deleteByCouponEventIdAndUserIsNull(Long eventId);

    int expireIssuedCouponsBefore(LocalDateTime now);
}
