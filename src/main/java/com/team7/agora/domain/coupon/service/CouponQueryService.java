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
 * 애플리케이션 유스케이스를 조정하는 서비스이다.
 */
@Service
@Transactional(readOnly = true)
public class CouponQueryService {

    private final CouponEventRepository couponEventRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final UserRepository userRepository;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param couponEventRepository 입력 값
     * @param couponIssueRepository 입력 값
     * @param userRepository 입력 값
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
     * 요청한 동작을 처리한다.
     * @return 처리 결과
     */
    public List<CouponEventResponse> listActiveEvents() {
        return couponEventRepository.findAllByStatus(CouponEventStatus.ACTIVE).stream()
            .map(CouponEventResponse::from)
            .toList();
    }

    /**
     * 데이터를 반환한다.
     * @param userId 입력 값
     * @return 처리 결과
     */
    public List<MyCouponResponse> getMyCoupons(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다."));

        return couponIssueRepository.findAllByUser(user).stream()
            .map(MyCouponResponse::from)
            .toList();
    }
}
