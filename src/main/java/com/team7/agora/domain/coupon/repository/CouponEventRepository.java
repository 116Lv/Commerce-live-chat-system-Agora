package com.team7.agora.domain.coupon.repository;

import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.enums.CouponEventStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CouponEventRepository {

    CouponEvent save(CouponEvent couponEvent);

    Optional<CouponEvent> findById(Long id);

    List<CouponEvent> findAll();

    List<CouponEvent> findAllByStatus(CouponEventStatus status);

    List<CouponEvent> findAllByStatusIn(Collection<CouponEventStatus> statuses);

    List<CouponEvent> findPublicIssueableEvents(LocalDateTime now);

    int endActiveEventsBefore(LocalDateTime now);
}
