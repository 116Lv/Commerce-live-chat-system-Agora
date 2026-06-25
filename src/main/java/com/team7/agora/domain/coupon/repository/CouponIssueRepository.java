package com.team7.agora.domain.coupon.repository;

import com.team7.agora.domain.coupon.entity.Coupon;
import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.entity.CouponIssue;
import com.team7.agora.domain.user.entity.User;
import java.util.List;

public interface CouponIssueRepository {

    CouponIssue save(CouponIssue couponIssue);

    boolean existsByCouponEventAndUser(CouponEvent couponEvent, User user);

    boolean existsByCouponAndUser(Coupon coupon, User user);

    List<CouponIssue> findAllByCoupon(Coupon coupon);

    List<CouponIssue> findAllByUser(User user);
}
