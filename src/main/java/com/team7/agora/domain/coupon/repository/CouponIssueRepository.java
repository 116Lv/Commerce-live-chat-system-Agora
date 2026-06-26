package com.team7.agora.domain.coupon.repository;

import com.team7.agora.domain.coupon.entity.Coupon;
import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.entity.CouponIssue;
import com.team7.agora.domain.user.entity.User;
import java.util.List;

/**
 * Coupon Issue 데이터 저장과 조회를 담당하는 저장소 인터페이스이다.
 */
public interface CouponIssueRepository {

    CouponIssue save(CouponIssue couponIssue);

    boolean existsByCouponEventAndUser(CouponEvent couponEvent, User user);

    boolean existsByCouponAndUser(Coupon coupon, User user);

    List<CouponIssue> findAllByCoupon(Coupon coupon);

    List<CouponIssue> findAllByUser(User user);
}
