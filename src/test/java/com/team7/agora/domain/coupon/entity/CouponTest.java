package com.team7.agora.domain.coupon.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.team7.agora.domain.coupon.enums.CouponEventType;
import com.team7.agora.domain.coupon.enums.CouponStatus;
import com.team7.agora.global.exception.BusinessException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class CouponTest {

    @Test
    void issuedCouponIsUsableUntilExpiresAtInclusive() {
        LocalDateTime issuedAt = LocalDateTime.of(2026, 6, 30, 12, 0);
        Coupon coupon = Coupon.createAvailableSlot(event());

        coupon.assign(null, issuedAt, 7);

        assertThat(coupon.isUsableAt(issuedAt.plusDays(7).minusNanos(1))).isTrue();
        assertThat(coupon.isUsableAt(issuedAt.plusDays(7))).isTrue();
    }

    @Test
    void useRejectsExpiredCouponAtUseTime() {
        LocalDateTime issuedAt = LocalDateTime.of(2026, 6, 30, 12, 0);
        Coupon coupon = Coupon.createAvailableSlot(event());
        coupon.assign(null, issuedAt, 7);

        assertThatThrownBy(() -> coupon.use(issuedAt.plusDays(7).plusNanos(1)))
            .isInstanceOf(BusinessException.class);
        assertThat(coupon.getStatus()).isEqualTo(CouponStatus.ISSUED);
    }

    @Test
    void couponIsNotUsableWhenNotIssuedOrExpired() {
        LocalDateTime issuedAt = LocalDateTime.of(2026, 6, 30, 12, 0);
        Coupon available = Coupon.createAvailableSlot(event());
        Coupon used = Coupon.createAvailableSlot(event());
        Coupon expired = Coupon.createAvailableSlot(event());

        used.assign(null, issuedAt, 7);
        used.use(issuedAt.plusDays(1));
        expired.assign(null, issuedAt, 7);

        assertThat(available.isUsableAt(issuedAt)).isFalse();
        assertThat(used.isUsableAt(issuedAt.plusDays(1))).isFalse();
        assertThat(expired.isUsableAt(issuedAt.plusDays(7).plusNanos(1))).isFalse();
    }

    private CouponEvent event() {
        return CouponEvent.create(
            CouponEventType.FIRST_COME,
            "First purchase coupon",
            5,
            LocalDateTime.of(2026, 6, 30, 0, 0),
            LocalDateTime.of(2026, 6, 30, 23, 59),
            5000,
            10000,
            30
        );
    }
}
