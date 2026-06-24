package com.team7.agora.domain.coupon.service;

import com.team7.agora.domain.coupon.dto.response.AdminCouponResponse;
import com.team7.agora.domain.coupon.entity.Coupon;
import com.team7.agora.domain.coupon.enums.CouponType;
import com.team7.agora.domain.coupon.repository.CouponRepository;
import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminCouponService {

    private final CouponRepository couponRepository;

    public AdminCouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Transactional
    public AdminCouponResponse create(
        CustomUserDetails admin,
        String name,
        int discountAmount,
        int minOrderAmount,
        CouponType type,
        int validDays
    ) {
        validateAdminAuthority(admin);
        Coupon coupon = couponRepository.save(Coupon.create(name, discountAmount, minOrderAmount, type, validDays));
        return AdminCouponResponse.from(coupon);
    }

    private void validateAdminAuthority(CustomUserDetails admin) {
        if (admin == null || !(admin.getRole() == UserRole.ROOT_ADMIN || admin.getRole() == UserRole.USER_ADMIN)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "쿠폰 정책 관리는 관리자만 수행할 수 있습니다.");
        }
    }
}
