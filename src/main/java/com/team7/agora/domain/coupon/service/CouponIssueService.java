package com.team7.agora.domain.coupon.service;

import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import com.team7.agora.global.lock.LockService;
import org.springframework.stereotype.Service;

@Service
public class CouponIssueService {

    private final UserRepository userRepository;
    private final LockService lockService;
    private final CouponIssueTransactionExecutor couponIssueTransactionExecutor;

    public CouponIssueService(
        UserRepository userRepository,
        LockService lockService,
        CouponIssueTransactionExecutor couponIssueTransactionExecutor
    ) {
        this.userRepository = userRepository;
        this.lockService = lockService;
        this.couponIssueTransactionExecutor = couponIssueTransactionExecutor;
    }

    public void issue(Long userId, Long couponEventId) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다.");
        }

        lockService.withLock("lock:coupon-event:" + couponEventId, () -> {
            couponIssueTransactionExecutor.issue(userId, couponEventId);
            return null;
        });
    }
}
