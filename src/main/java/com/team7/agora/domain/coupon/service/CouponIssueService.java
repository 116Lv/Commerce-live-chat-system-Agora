package com.team7.agora.domain.coupon.service;

import com.team7.agora.global.lock.LockService;
import org.springframework.stereotype.Service;

/**
 * Coupon Issue 관련 비즈니스 유스케이스를 처리하는 서비스이다.
 */
@Service
public class CouponIssueService {

    private final LockService lockService;
    private final CouponIssueTransactionExecutor couponIssueTransactionExecutor;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param lockService 쿠폰 발급 중복 실행을 막기 위해 락을 거는 서비스
     * @param couponIssueTransactionExecutor 쿠폰 발급 트랜잭션을 실제로 수행하는 실행기
     */
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
