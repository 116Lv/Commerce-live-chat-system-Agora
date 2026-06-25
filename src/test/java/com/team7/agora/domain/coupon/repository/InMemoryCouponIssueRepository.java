package com.team7.agora.domain.coupon.repository;

import com.team7.agora.domain.coupon.entity.Coupon;
import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.entity.CouponIssue;
import com.team7.agora.domain.user.entity.User;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryCouponIssueRepository implements CouponIssueRepository {

    private final List<CouponIssue> issues = new CopyOnWriteArrayList<>();
    private final AtomicInteger count = new AtomicInteger();

    @Override
    public CouponIssue save(CouponIssue couponIssue) {
        count.incrementAndGet();
        issues.add(couponIssue);
        return couponIssue;
    }

    @Override
    public boolean existsByCouponEventAndUser(CouponEvent couponEvent, User user) {
        return issues.stream()
            .anyMatch(issue -> issue.getCouponEvent() == couponEvent && issue.getUser() == user);
    }

    @Override
    public boolean existsByCouponAndUser(Coupon coupon, User user) {
        return issues.stream()
            .anyMatch(issue -> issue.getCoupon() == coupon && issue.getUser() == user);
    }

    @Override
    public List<CouponIssue> findAllByUser(User user) {
        return List.of();
    }

    public int count() {
        return count.get();
    }
}
