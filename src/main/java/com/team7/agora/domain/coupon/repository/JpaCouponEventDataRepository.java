package com.team7.agora.domain.coupon.repository;

import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.enums.CouponEventStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface JpaCouponEventDataRepository extends JpaRepository<CouponEvent, Long> {

    List<CouponEvent> findAllByStatus(CouponEventStatus status);
}
