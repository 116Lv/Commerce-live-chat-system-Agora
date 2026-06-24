package com.team7.agora.domain.coupon.service;

import static com.team7.agora.support.TestEntityIds.assignId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.coupon.dto.response.AdminCouponResponse;
import com.team7.agora.domain.coupon.entity.Coupon;
import com.team7.agora.domain.coupon.enums.CouponType;
import com.team7.agora.domain.coupon.repository.CouponRepository;
import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminCouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Test
    void createSavesCouponPolicy() {
        AdminCouponService service = new AdminCouponService(couponRepository);
        when(couponRepository.save(any(Coupon.class))).thenAnswer(invocation -> {
            Coupon coupon = invocation.getArgument(0);
            assignId(coupon, 1L);
            return coupon;
        });

        AdminCouponResponse response = service.create(
            principal(UserRole.ROOT_ADMIN),
            "신규 쿠폰",
            5000,
            10000,
            CouponType.FIRST_COME,
            30
        );

        assertThat(response.couponId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("신규 쿠폰");
        assertThat(response.type()).isEqualTo("FIRST_COME");
    }

    @Test
    void getDetailReturnsCouponPolicy() {
        AdminCouponService service = new AdminCouponService(couponRepository);
        Coupon coupon = Coupon.create("신규 쿠폰", 5000, 10000, CouponType.FIRST_COME, 30);
        assignId(coupon, 1L);
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));

        AdminCouponResponse response = service.getDetail(principal(UserRole.USER_ADMIN), 1L);

        assertThat(response.couponId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("신규 쿠폰");
        assertThat(response.type()).isEqualTo("FIRST_COME");
    }

    @Test
    void getDetailRejectsMissingCoupon() {
        AdminCouponService service = new AdminCouponService(couponRepository);
        when(couponRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDetail(principal(UserRole.ROOT_ADMIN), 1L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void getDetailRejectsNonAdmin() {
        AdminCouponService service = new AdminCouponService(couponRepository);

        assertThatThrownBy(() -> service.getDetail(principal(UserRole.ROLE_USER), 1L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void createRejectsNonAdmin() {
        AdminCouponService service = new AdminCouponService(couponRepository);

        assertThatThrownBy(() -> service.create(
            principal(UserRole.ROLE_USER),
            "신규 쿠폰",
            5000,
            10000,
            CouponType.FIRST_COME,
            30
        ))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.FORBIDDEN);
    }

    private CustomUserDetails principal(UserRole role) {
        return new CustomUserDetails(99L, "admin@test.com", "encoded", role, UserStatus.ACTIVE, "관리자");
    }
}
