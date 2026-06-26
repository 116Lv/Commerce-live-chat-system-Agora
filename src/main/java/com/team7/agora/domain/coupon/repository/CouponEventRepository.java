package com.team7.agora.domain.coupon.repository;

import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.enums.CouponEventStatus;
import java.util.List;
import java.util.Optional;

/**
 * 쿠폰 이벤트 데이터 저장과 조회를 담당하는 저장소 인터페이스이다.
 */
public interface CouponEventRepository {

    CouponEvent save(CouponEvent couponEvent);

    Optional<CouponEvent> findById(Long id);

    List<CouponEvent> findAllByStatus(CouponEventStatus status);
}
