package com.team7.agora.domain.coupon.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.team7.agora.global.lock.LockService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CouponIssueServiceTest {

    @Mock
    private CouponIssueTransactionExecutor couponIssueTransactionExecutor;

    @Test
    void issue_delegatesToTransactionExecutorWithinLock() {
        CouponIssueService service = new CouponIssueService(LockService.local(), couponIssueTransactionExecutor);

        service.issue(1L, 1L);

        verify(couponIssueTransactionExecutor, times(1)).issue(1L, 1L);
    }
}
