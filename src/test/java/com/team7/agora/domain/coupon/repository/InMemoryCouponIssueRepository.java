package com.team7.agora.domain.coupon.repository;

import com.team7.agora.domain.coupon.entity.Coupon;
import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.entity.CouponIssue;
import com.team7.agora.domain.user.entity.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryCouponIssueRepository implements CouponIssueRepository {

    private final Set<String> issuedKeys = ConcurrentHashMap.newKeySet();
    private final List<CouponIssue> issues = new ArrayList<>();
    private final AtomicInteger count = new AtomicInteger();

    @Override
    public CouponIssue save(CouponIssue couponIssue) {
        count.incrementAndGet();
        issues.add(couponIssue);
        return couponIssue;
    }

    @Override
    public boolean existsByCouponEventAndUser(CouponEvent couponEvent, User user) {
        return !issuedKeys.add(System.identityHashCode(couponEvent) + ":" + System.identityHashCode(user));
    }

    @Override
    public List<CouponIssue> findAllByCoupon(Coupon coupon) {
        return issues.stream()
            .filter(issue -> issue.getCoupon() == coupon)
            .toList();
    }

    @Override
    public List<CouponIssue> findAllByUser(User user) {
        return List.of();
    }

    public int count() {
        return count.get();
    }
}
