package com.team7.agora.domain.coupon.service;

import com.team7.agora.domain.coupon.dto.response.CouponEventResponse;
import com.team7.agora.domain.coupon.dto.response.MyCouponResponse;
import com.team7.agora.domain.coupon.enums.CouponStatus;
import com.team7.agora.domain.coupon.repository.CouponEventRepository;
import com.team7.agora.domain.coupon.repository.CouponRepository;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CouponQueryService {

    private final CouponEventRepository couponEventRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    public CouponQueryService(
        CouponEventRepository couponEventRepository,
        CouponRepository couponRepository,
        UserRepository userRepository
    ) {
        this.couponEventRepository = couponEventRepository;
        this.couponRepository = couponRepository;
        this.userRepository = userRepository;
    }

    public List<CouponEventResponse> listActiveEvents() {
        return couponEventRepository.findPublicIssueableEvents(LocalDateTime.now()).stream()
            .map(CouponEventResponse::from)
            .toList();
    }

    public List<MyCouponResponse> getMyCoupons(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다.");
        }

        return couponRepository.findAllByUserIdAndStatusIn(userId, List.of(CouponStatus.ISSUED, CouponStatus.EXPIRED))
            .stream()
            .map(MyCouponResponse::from)
            .toList();
    }
}
