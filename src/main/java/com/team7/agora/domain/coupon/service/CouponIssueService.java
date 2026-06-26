package com.team7.agora.domain.coupon.service;

import com.team7.agora.global.lock.LockService;
import org.springframework.stereotype.Service;

/**
 * 애플리케이션 유스케이스를 조정하는 서비스이다.
 */
@Service
public class CouponIssueService {

    private final LockService lockService;
    private final CouponIssueTransactionExecutor couponIssueTransactionExecutor;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param couponEventRepository 입력 값
     * @param couponRepository 입력 값
     * @param couponIssueRepository 입력 값
     * @param userRepository 입력 값
     * @param lockService 입력 값
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
