package com.team7.agora.domain.coupon.service;

import static com.team7.agora.support.TestEntityIds.assignId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.coupon.entity.Coupon;
import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.enums.CouponEventType;
import com.team7.agora.domain.coupon.enums.CouponStatus;
import com.team7.agora.domain.coupon.repository.CouponEventRepository;
import com.team7.agora.domain.coupon.repository.CouponRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CouponSlotTransactionExecutorTest {

    @Mock
    private CouponEventRepository couponEventRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void assignSlotFillsAvailableCouponSlot() {
        CouponEvent event = event();
        User user = user(10L);
        Coupon slot = Coupon.createAvailableSlot(event);
        assignId(slot, 100L);
        when(couponEventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(10L, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(couponRepository.existsByCouponEventIdAndUserId(1L, 10L)).thenReturn(false);
        when(couponRepository.findFirstAvailableSlotForUpdate(1L)).thenReturn(Optional.of(slot));

        var response = newExecutor().assignSlotInTransaction(1L, 10L);

        assertThat(response.issuedCount()).isEqualTo(1);
        assertThat(slot.getUser()).isSameAs(user);
        assertThat(slot.getStatus()).isEqualTo(CouponStatus.ISSUED);
        assertThat(slot.getExpiresAt()).isAfter(slot.getIssuedAt());
        assertThat(event.getIssuedQuantity()).isEqualTo(1);
    }

    @Test
    void assignSlotRejectsDuplicateUserForEvent() {
        CouponEvent event = event();
        User user = user(10L);
        when(couponEventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(10L, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(couponRepository.existsByCouponEventIdAndUserId(1L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> newExecutor().assignSlotInTransaction(1L, 10L))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void assignSlotsSkipsDuplicatedUsers() {
        CouponEvent event = event();
        User first = user(10L);
        User duplicated = user(11L);
        Coupon slot = Coupon.createAvailableSlot(event);
        assignId(slot, 100L);
        when(couponEventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(10L, UserStatus.ACTIVE)).thenReturn(Optional.of(first));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(11L, UserStatus.ACTIVE)).thenReturn(Optional.of(duplicated));
        when(couponRepository.existsByCouponEventIdAndUserId(1L, 10L)).thenReturn(false);
        when(couponRepository.existsByCouponEventIdAndUserId(1L, 11L)).thenReturn(true);
        when(couponRepository.findFirstAvailableSlotForUpdate(1L)).thenReturn(Optional.of(slot));

        var response = newExecutor().assignSlotsInTransaction(1L, java.util.List.of(10L, 11L));

        assertThat(response.issuedCount()).isEqualTo(1);
        assertThat(response.skippedCount()).isEqualTo(1);
        verify(couponRepository).findFirstAvailableSlotForUpdate(1L);
    }

    @Test
    void assignSlotRejectsNonActiveUser() {
        CouponEvent event = event();
        when(couponEventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(10L, UserStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newExecutor().assignSlotInTransaction(1L, 10L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.NOT_FOUND);
    }

    private CouponSlotTransactionExecutor newExecutor() {
        return new CouponSlotTransactionExecutor(couponEventRepository, couponRepository, userRepository);
    }

    private CouponEvent event() {
        CouponEvent event = CouponEvent.create(
            CouponEventType.FIRST_COME,
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

    private User user(Long id) {
        User user = User.signup("user" + id + "@test.com", "encoded", "user" + id, "01012345678");
        assignId(user, id);
        return user;
    }
}
