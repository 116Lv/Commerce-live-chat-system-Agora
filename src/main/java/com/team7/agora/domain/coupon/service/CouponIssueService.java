package com.team7.agora.domain.coupon.service;

import com.team7.agora.global.lock.LockService;
import org.springframework.stereotype.Service;

@Service
public class CouponIssueService {

    private final LockService lockService;
    private final CouponIssueTransactionExecutor couponIssueTransactionExecutor;

    public CouponIssueService(
        LockService lockService,
        CouponIssueTransactionExecutor couponIssueTransactionExecutor
    ) {
        this.lockService = lockService;
        this.couponIssueTransactionExecutor = couponIssueTransactionExecutor;
    }

    public void issue(Long userId, Long couponEventId) {
        lockService.withLock("lock:coupon-event:" + couponEventId, () -> {
            couponIssueTransactionExecutor.issue(userId, couponEventId);
            return null;
        });
    }
}
