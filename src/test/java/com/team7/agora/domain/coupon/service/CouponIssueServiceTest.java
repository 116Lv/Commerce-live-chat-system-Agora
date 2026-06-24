package com.team7.agora.domain.coupon.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.repository.CouponEventRepository;
import com.team7.agora.domain.coupon.repository.CouponIssueRepository;
import com.team7.agora.domain.coupon.repository.CouponRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import com.team7.agora.global.lock.LockService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CouponIssueServiceTest {

    @Mock
    private CouponEventRepository couponEventRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponIssueRepository couponIssueRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void issue_throwsConflictWhenSoldOut() {
        CouponIssueService service = new CouponIssueService(
            couponEventRepository,
            couponRepository,
            couponIssueRepository,
            userRepository,
            LockService.local()
        );
        CouponEvent event = CouponEvent.create("동네 쿠폰", 1, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        event.issue();
        when(couponEventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(1L)).thenReturn(Optional.of(User.signup("user@test.com", "encoded", "동네유저", "01012345678")));

        assertThatThrownBy(() -> service.issue(1L, 1L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.CONFLICT);
    }

    @Test
    void issue_throwsConflictWhenUserAlreadyIssued() {
        CouponIssueService service = new CouponIssueService(
            couponEventRepository,
            couponRepository,
            couponIssueRepository,
            userRepository,
            LockService.local()
        );
        CouponEvent event = CouponEvent.create("동네 쿠폰", 10, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        User user = User.signup("user@test.com", "encoded", "동네유저", "01012345678");
        when(couponEventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(couponIssueRepository.existsByCouponEventAndUser(event, user)).thenReturn(true);

        assertThatThrownBy(() -> service.issue(1L, 1L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.CONFLICT);
    }
}
