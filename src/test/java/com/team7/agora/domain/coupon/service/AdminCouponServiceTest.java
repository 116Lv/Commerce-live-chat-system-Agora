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
import java.util.List;
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
    void listReturnsAllCouponPolicies() {
        AdminCouponService service = new AdminCouponService(couponRepository);
        Coupon firstCome = Coupon.create("신규 쿠폰", 5000, 10000, CouponType.FIRST_COME, 30);
        Coupon smileReward = Coupon.create("스마일 보상 쿠폰", 3000, 5000, CouponType.SMILE_REWARD, 14);
        assignId(firstCome, 1L);
        assignId(smileReward, 2L);
        when(couponRepository.findAll()).thenReturn(List.of(firstCome, smileReward));

        List<AdminCouponResponse> responses = service.list(principal(UserRole.USER_ADMIN));

        assertThat(responses).hasSize(2);
        assertThat(responses)
            .extracting(AdminCouponResponse::couponId)
            .containsExactly(1L, 2L);
    }

    @Test
    void listRejectsNonAdmin() {
        AdminCouponService service = new AdminCouponService(couponRepository);

        assertThatThrownBy(() -> service.list(principal(UserRole.ROLE_USER)))
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
