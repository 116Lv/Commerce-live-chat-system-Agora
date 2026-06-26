package com.team7.agora.domain.coupon.service;

import static com.team7.agora.support.TestEntityIds.assignId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.enums.CouponEventType;
import com.team7.agora.domain.coupon.repository.CouponEventRepository;
import com.team7.agora.domain.coupon.repository.CouponRepository;
import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.exception.BusinessException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.stream.LongStream;

@ExtendWith(MockitoExtension.class)
class AdminCouponEventServiceTest {

    @Mock
    private CouponEventRepository couponEventRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponSlotService couponSlotService;

    @Test
    void createEventCreatesSlotsByTotalQuantity() {
        when(couponEventRepository.save(any(CouponEvent.class))).thenAnswer(invocation -> {
            CouponEvent event = invocation.getArgument(0);
            assignId(event, 1L);
            return event;
        });
        ArgumentCaptor<java.util.List> slotsCaptor = ArgumentCaptor.forClass(java.util.List.class);

        var response = newService().createEvent(
            principal(UserRole.ROOT_ADMIN),
            CouponEventType.FIRST_COME,
            "첫 거래 쿠폰",
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(1),
            3,
            5000,
            10000,
            30
        );

        org.mockito.Mockito.verify(couponRepository).saveAll(slotsCaptor.capture());
        assertThat(response.eventId()).isEqualTo(1L);
        assertThat(response.type()).isEqualTo("FIRST_COME");
        assertThat(slotsCaptor.getValue()).hasSize(3);
    }

    @Test
    void createEventRejectsNonAdmin() {
        assertThatThrownBy(() -> newService().createEvent(
            principal(UserRole.ROLE_USER),
            CouponEventType.FIRST_COME,
            "첫 거래 쿠폰",
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(1),
            3,
            5000,
            10000,
            30
        )).isInstanceOf(BusinessException.class);
    }

    @Test
    void issueToUsersRejectsTooManyTargets() {
        assertThatThrownBy(() -> newService().issueToUsers(
            principal(UserRole.ROOT_ADMIN),
            1L,
            LongStream.rangeClosed(1, 101).boxed().toList()
        )).isInstanceOf(BusinessException.class);
    }

    private AdminCouponEventService newService() {
        return new AdminCouponEventService(couponEventRepository, couponRepository, couponSlotService);
    }

    private CustomUserDetails principal(UserRole role) {
        return new CustomUserDetails(99L, "admin@test.com", "encoded", role, UserStatus.ACTIVE, "admin");
    }
}
