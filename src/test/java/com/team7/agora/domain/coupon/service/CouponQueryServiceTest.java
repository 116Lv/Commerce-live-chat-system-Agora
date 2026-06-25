package com.team7.agora.domain.coupon.service;

import static com.team7.agora.support.TestEntityIds.assignId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.coupon.dto.response.CouponEventResponse;
import com.team7.agora.domain.coupon.dto.response.MyCouponResponse;
import com.team7.agora.domain.coupon.entity.Coupon;
import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.entity.CouponIssue;
import com.team7.agora.domain.coupon.enums.CouponEventStatus;
import com.team7.agora.domain.coupon.repository.CouponEventRepository;
import com.team7.agora.domain.coupon.repository.CouponIssueRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.exception.BusinessException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CouponQueryServiceTest {

    @Mock
    private CouponEventRepository couponEventRepository;

    @Mock
    private CouponIssueRepository couponIssueRepository;

    @Mock
    private UserRepository userRepository;

    private CouponQueryService newService() {
        return new CouponQueryService(couponEventRepository, couponIssueRepository, userRepository);
    }

    @Test
    void listActiveEventsReturnsActiveCouponEvents() {
        CouponEvent event = CouponEvent.create(
            "오픈 기념 쿠폰",
            100,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(7)
        );
        assignId(event, 1L);
        when(couponEventRepository.findAllByStatus(CouponEventStatus.ACTIVE)).thenReturn(List.of(event));

        List<CouponEventResponse> responses = newService().listActiveEvents();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).eventId()).isEqualTo(1L);
    }

    @Test
    void getMyCouponsReturnsIssuedCoupons() {
        User user = User.signup("user@test.com", "password", "동네유저", "01012345678");
        assignId(user, 1L);
        Coupon coupon = Coupon.firstCome("환영 쿠폰", 3000, 10000, 30);
        assignId(coupon, 10L);
        CouponEvent event = CouponEvent.create("이벤트", 10, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        CouponIssue issue = CouponIssue.issue(coupon, event, user);
        assignId(issue, 100L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(couponIssueRepository.findAllByUser(user)).thenReturn(List.of(issue));

        List<MyCouponResponse> responses = newService().getMyCoupons(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).issueId()).isEqualTo(100L);
        assertThat(responses.get(0).couponId()).isEqualTo(10L);
        assertThat(responses.get(0).couponName()).isEqualTo("환영 쿠폰");
    }

    @Test
    void getMyCouponsRejectsMissingUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().getMyCoupons(1L))
            .isInstanceOf(BusinessException.class);
    }
}
