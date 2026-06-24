package com.team7.agora.domain.coupon.repository;

import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.entity.CouponIssue;
import com.team7.agora.domain.user.entity.User;
import org.springframework.stereotype.Repository;

@Repository
class CouponIssueRepositoryAdapter implements CouponIssueRepository {

    private final JpaCouponIssueDataRepository repository;

    CouponIssueRepositoryAdapter(JpaCouponIssueDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public CouponIssue save(CouponIssue couponIssue) {
        return repository.save(couponIssue);
    }

    @Override
    public boolean existsByCouponEventAndUser(CouponEvent couponEvent, User user) {
        return repository.existsByCouponEventAndUser(couponEvent, user);
    }
}
