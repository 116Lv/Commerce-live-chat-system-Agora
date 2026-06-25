package com.team7.agora.domain.coupon.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import com.team7.agora.global.lock.LockService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CouponIssueServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CouponIssueTransactionExecutor couponIssueTransactionExecutor;

    @Test
    void issue_throwsNotFoundWhenUserDoesNotExist() {
        CouponIssueService service = new CouponIssueService(userRepository, LockService.local(), couponIssueTransactionExecutor);
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> service.issue(1L, 1L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void issue_delegatesToTransactionExecutorWithinLock() {
        CouponIssueService service = new CouponIssueService(userRepository, LockService.local(), couponIssueTransactionExecutor);
        when(userRepository.existsById(1L)).thenReturn(true);

        service.issue(1L, 1L);

        verify(couponIssueTransactionExecutor, times(1)).issue(1L, 1L);
    }
}
