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
     * @param couponEventRepository 데이터를 조회하고 저장하는 리포지토리
     * @param couponRepository 데이터를 조회하고 저장하는 리포지토리
     * @param couponIssueRepository 데이터를 조회하고 저장하는 리포지토리
     * @param userRepository 데이터를 조회하고 저장하는 리포지토리
     * @param lockService 해당 기능의 비즈니스 로직을 처리하는 서비스
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
