package com.team7.agora.domain.coupon.repository;

import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.enums.CouponEventStatus;
import java.util.List;
import java.util.Optional;

/**
 * 데이터 저장과 조회를 위한 저장소 계약이다.
 */
public interface CouponEventRepository {

    CouponEvent save(CouponEvent couponEvent);

    Optional<CouponEvent> findById(Long id);

    List<CouponEvent> findAllByStatus(CouponEventStatus status);
}
