package com.team7.agora.domain.coupon.repository;

import com.team7.agora.domain.coupon.entity.CouponEvent;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CouponEventRepository {

    CouponEvent save(CouponEvent couponEvent);

    Optional<CouponEvent> findById(Long id);

    List<CouponEvent> findAll();

    List<CouponEvent> findPublicIssueableEvents(LocalDateTime now);
}
