package com.team7.agora.domain.coupon.service;

import com.team7.agora.domain.coupon.dto.response.AdminCouponResponse;
import com.team7.agora.domain.coupon.dto.response.CouponBroadcastResponse;
import com.team7.agora.domain.coupon.entity.Coupon;
import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.entity.CouponIssue;
import com.team7.agora.domain.coupon.enums.CouponType;
import com.team7.agora.domain.coupon.repository.CouponEventRepository;
import com.team7.agora.domain.coupon.repository.CouponIssueRepository;
import com.team7.agora.domain.coupon.repository.CouponRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminCouponService {

    private final CouponRepository couponRepository;
    private final CouponEventRepository couponEventRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final UserRepository userRepository;

    public AdminCouponService(
        CouponRepository couponRepository,
        CouponEventRepository couponEventRepository,
        CouponIssueRepository couponIssueRepository,
        UserRepository userRepository
    ) {
        this.couponRepository = couponRepository;
        this.couponEventRepository = couponEventRepository;
        this.couponIssueRepository = couponIssueRepository;
        this.userRepository = userRepository;
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

    public AdminCouponResponse getDetail(CustomUserDetails admin, Long couponId) {
        validateAdminAuthority(admin);
        Coupon coupon = couponRepository.findById(couponId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "쿠폰 정책을 찾을 수 없습니다."));
        return AdminCouponResponse.from(coupon);
    }

    @Transactional
    public CouponBroadcastResponse issueToUsers(CustomUserDetails admin, Long couponId, List<Long> userIds) {
        validateAdminAuthority(admin);
        Coupon coupon = couponRepository.findById(couponId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "쿠폰 정책을 찾을 수 없습니다."));

        List<User> targets = userRepository.findAllById(userIds);
        if (targets.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "발급 대상 사용자가 없습니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        CouponEvent event = couponEventRepository.save(CouponEvent.create(
            coupon.getName() + " 지정 발급",
            targets.size(),
            now.minusMinutes(1),
            now.plusDays(coupon.getValidDays())
        ));

        int issuedCount = 0;
        int skippedCount = 0;
        for (User user : targets) {
            if (couponIssueRepository.existsByCouponAndUser(coupon, user)) {
                skippedCount++;
                continue;
            }
            event.issue();
            couponIssueRepository.save(CouponIssue.issue(coupon, event, user));
            issuedCount++;
        }

        return new CouponBroadcastResponse(couponId, issuedCount, skippedCount);
    }

    private void validateAdminAuthority(CustomUserDetails admin) {
        if (admin == null || !(admin.getRole() == UserRole.ROOT_ADMIN || admin.getRole() == UserRole.USER_ADMIN)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "쿠폰 정책 관리는 관리자만 수행할 수 있습니다.");
        }
    }
}
