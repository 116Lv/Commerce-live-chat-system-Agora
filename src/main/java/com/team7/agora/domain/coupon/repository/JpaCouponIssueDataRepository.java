package com.team7.agora.domain.coupon.repository;

import com.team7.agora.domain.coupon.entity.Coupon;
import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.entity.CouponIssue;
import com.team7.agora.domain.user.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface JpaCouponIssueDataRepository extends JpaRepository<CouponIssue, Long> {

    boolean existsByCouponEventAndUser(CouponEvent couponEvent, User user);

    boolean existsByCouponAndUser(Coupon coupon, User user);

    List<CouponIssue> findAllByUser(User user);
}
