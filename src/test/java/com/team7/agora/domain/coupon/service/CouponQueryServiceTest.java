package com.team7.agora.domain.coupon.service;

import static com.team7.agora.support.TestEntityIds.assignId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.coupon.entity.Coupon;
import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.enums.CouponEventType;
import com.team7.agora.domain.coupon.enums.CouponStatus;
import com.team7.agora.domain.coupon.repository.CouponEventRepository;
import com.team7.agora.domain.coupon.repository.CouponRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CouponQueryServiceTest {

    @Mock
    private CouponEventRepository couponEventRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void listActiveEventsReturnsPublicIssueableEvents() {
        CouponEvent event = event(CouponEventType.FIRST_COME);
        when(couponEventRepository.findPublicIssueableEvents(any(LocalDateTime.class))).thenReturn(List.of(event));

        var responses = newService().listActiveEvents();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).type()).isEqualTo("FIRST_COME");
        assertThat(responses.get(0).discountAmount()).isEqualTo(5000);
        assertThat(responses.get(0).remainingQuantity()).isEqualTo(5);
        assertThat(responses.get(0).issueRate()).isEqualTo(0.0);
        assertThat(responses.get(0).canIssue()).isTrue();
        assertThat(responses.get(0).ended()).isFalse();
        assertThat(responses.get(0).soldOut()).isFalse();
        assertThat(responses.get(0).statusLabel()).isEqualTo("Active");
        assertThat(responses.get(0).typeLabel()).isEqualTo("First come");
    }

    @Test
    void getMyCouponsReturnsAssignedCouponSlots() {
        CouponEvent event = event(CouponEventType.FIRST_COME);
        User user = User.signup("user@test.com", "encoded", "user", "01012345678");
        assignId(user, 10L);
        Coupon coupon = Coupon.createAvailableSlot(event);
        assignId(coupon, 100L);
        coupon.assign(user, LocalDateTime.now(), event.getValidDays());
        when(userRepository.existsById(10L)).thenReturn(true);
        when(couponRepository.findAllByUserIdAndStatusIn(
                10L,
                List.of(CouponStatus.ISSUED, CouponStatus.USED, CouponStatus.EXPIRED)
            ))
            .thenReturn(List.of(coupon));

        var responses = newService().getMyCoupons(10L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).couponId()).isEqualTo(100L);
        assertThat(responses.get(0).expiresAt()).isNotNull();
        assertThat(responses.get(0).statusLabel()).isEqualTo("Issued");
        assertThat(responses.get(0).usable()).isTrue();
        assertThat(responses.get(0).expired()).isFalse();
    }

    private CouponQueryService newService() {
        return new CouponQueryService(couponEventRepository, couponRepository, userRepository);
    }

    private CouponEvent event(CouponEventType type) {
        CouponEvent event = CouponEvent.create(
            type,
            "첫 거래 쿠폰",
            5,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(1),
            5000,
            10000,
            30
        );
        assignId(event, 1L);
        return event;
    }
}
