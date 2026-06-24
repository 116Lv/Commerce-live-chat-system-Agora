package com.team7.agora.domain.coupon.repository;

import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.enums.CouponEventStatus;
import java.util.List;
import java.util.Optional;

public interface CouponEventRepository {

    CouponEvent save(CouponEvent couponEvent);

    Optional<CouponEvent> findById(Long id);

    List<CouponEvent> findAllByStatus(CouponEventStatus status);
}
