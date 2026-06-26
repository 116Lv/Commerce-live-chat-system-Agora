package com.team7.agora.domain.coupon.service;

import com.team7.agora.domain.coupon.dto.response.CouponEventResponse;
import com.team7.agora.domain.coupon.dto.response.MyCouponResponse;
import com.team7.agora.domain.coupon.enums.CouponEventStatus;
import com.team7.agora.domain.coupon.repository.CouponEventRepository;
import com.team7.agora.domain.coupon.repository.CouponIssueRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service that coordinates coupon query use cases.
 */
@Service
@Transactional(readOnly = true)
public class CouponQueryService {

    private final CouponEventRepository couponEventRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final UserRepository userRepository;

    /**
     * Creates a coupon query service instance.
     * @param couponEventRepository the coupon event repository value
     * @param couponIssueRepository the coupon issue repository value
     * @param userRepository the user repository value
     */
    public CouponQueryService(
        CouponEventRepository couponEventRepository,
        CouponIssueRepository couponIssueRepository,
        UserRepository userRepository
    ) {
        this.couponEventRepository = couponEventRepository;
        this.couponIssueRepository = couponIssueRepository;
        this.userRepository = userRepository;
    }

    /**
     * Handles list active events behavior.
     * @return the list active events result
     */
    public List<CouponEventResponse> listActiveEvents() {
        return couponEventRepository.findAllByStatus(CouponEventStatus.ACTIVE).stream()
            .map(CouponEventResponse::from)
            .toList();
    }

    /**
     * Returns my coupons data.
     * @param userId the user id value
     * @return the get my coupons result
     */
    public List<MyCouponResponse> getMyCoupons(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다."));

        return couponIssueRepository.findAllByUser(user).stream()
            .map(MyCouponResponse::from)
            .toList();
    }
}
