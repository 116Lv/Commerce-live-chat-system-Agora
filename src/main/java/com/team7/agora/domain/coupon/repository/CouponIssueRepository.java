package com.team7.agora.domain.coupon.repository;

import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.entity.CouponIssue;
import com.team7.agora.domain.user.entity.User;

public interface CouponIssueRepository {

    CouponIssue save(CouponIssue couponIssue);

    boolean existsByCouponEventAndUser(CouponEvent couponEvent, User user);
}
