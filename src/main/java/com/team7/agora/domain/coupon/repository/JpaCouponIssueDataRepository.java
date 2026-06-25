package com.team7.agora.domain.coupon.repository;

import com.team7.agora.domain.coupon.entity.Coupon;
import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.entity.CouponIssue;
import com.team7.agora.domain.user.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface JpaCouponIssueDataRepository extends JpaRepository<CouponIssue, Long> {

    boolean existsByCouponEventAndUser(CouponEvent couponEvent, User user);

    boolean existsByCouponAndUser(Coupon coupon, User user);

    @Query("select ci from CouponIssue ci join fetch ci.user where ci.coupon = :coupon order by ci.issuedAt desc")
    List<CouponIssue> findAllByCoupon(@Param("coupon") Coupon coupon);

    List<CouponIssue> findAllByUser(User user);
}
