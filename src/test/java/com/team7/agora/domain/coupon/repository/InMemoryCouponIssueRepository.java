package com.team7.agora.domain.coupon.repository;

import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.entity.CouponIssue;
import com.team7.agora.domain.user.entity.User;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryCouponIssueRepository implements CouponIssueRepository {

    private final Set<String> issuedKeys = ConcurrentHashMap.newKeySet();
    private final AtomicInteger count = new AtomicInteger();

    @Override
    public CouponIssue save(CouponIssue couponIssue) {
        count.incrementAndGet();
        return couponIssue;
    }

    @Override
    public boolean existsByCouponEventAndUser(CouponEvent couponEvent, User user) {
        return !issuedKeys.add(System.identityHashCode(couponEvent) + ":" + System.identityHashCode(user));
    }

    public int count() {
        return count.get();
    }
}
