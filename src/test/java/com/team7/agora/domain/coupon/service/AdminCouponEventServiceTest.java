package com.team7.agora.domain.coupon.service;

import static com.team7.agora.support.TestEntityIds.assignId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.coupon.entity.Coupon;
import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.enums.CouponEventType;
import com.team7.agora.domain.coupon.repository.CouponEventRepository;
import com.team7.agora.domain.coupon.repository.CouponRepository;
import com.team7.agora.domain.admin.enums.AdminRole;
import com.team7.agora.domain.admin.enums.AdminStatus;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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
            principal(AdminRole.ROOT_ADMIN),
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
        assertThat(response.remainingQuantity()).isEqualTo(3);
        assertThat(response.issueRate()).isEqualTo(0.0);
        assertThat(response.canIssue()).isTrue();
        assertThat(response.statusLabel()).isEqualTo("Active");
        assertThat(response.typeLabel()).isEqualTo("First come");
        assertThat(slotsCaptor.getValue()).hasSize(3);
    }

    @Test
    void createEventRejectsNonAdmin() {
        assertThatThrownBy(() -> newService().createEvent(
            principal(AdminRole.PRODUCT_ADMIN),
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
            principal(AdminRole.ROOT_ADMIN),
            1L,
            LongStream.rangeClosed(1, 101).boxed().toList()
        )).isInstanceOf(BusinessException.class);
    }

    @Test
    void issueToUsersRejectsEmptyTargetsBeforeLookingUpEvent() {
        assertThatThrownBy(() -> newService().issueToUsers(
            principal(AdminRole.ROOT_ADMIN),
            1L,
            List.of()
        ))
            .isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST)
            );

        verify(couponEventRepository, never()).findById(any());
    }

    @Test
    void issueToUsersDoesNotStartWriteTransactionBeforeLockDelegation() throws NoSuchMethodException {
        Transactional transactional = AdminCouponEventService.class
            .getMethod("issueToUsers", AdminPrincipal.class, Long.class, List.class)
            .getAnnotation(Transactional.class);

        assertThat(transactional).isNotNull();
        assertThat(transactional.propagation()).isEqualTo(Propagation.NOT_SUPPORTED);
    }

    @Test
    void getCouponsIncludesUserEmailAndReadableLabels() {
        CouponEvent event = event(CouponEventType.ADMIN_INDIVIDUAL);
        User user = User.signup("buyer@test.com", "encoded", "buyer", "01012345678");
        assignId(user, 10L);
        Coupon coupon = Coupon.createAvailableSlot(event);
        assignId(coupon, 100L);
        coupon.assign(user, LocalDateTime.now(), event.getValidDays());

        when(couponEventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(couponRepository.findAllByCouponEventIdAndUserIsNotNull(1L)).thenReturn(List.of(coupon));

        var responses = newService().getCoupons(principal(AdminRole.ROOT_ADMIN), 1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).userNickname()).isEqualTo("buyer");
        assertThat(responses.get(0).userEmail()).isEqualTo("buyer@test.com");
        assertThat(responses.get(0).statusLabel()).isEqualTo("Issued");
    }

    private AdminCouponEventService newService() {
        return new AdminCouponEventService(couponEventRepository, couponRepository, couponSlotService);
    }

    private CouponEvent event(CouponEventType type) {
        CouponEvent event = CouponEvent.create(
            type,
            "Admin coupon",
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

    private AdminPrincipal principal(AdminRole role) {
        return new AdminPrincipal(99L, "admin@test.com", "encoded", role, AdminStatus.ACTIVE, "admin");
    }
}
