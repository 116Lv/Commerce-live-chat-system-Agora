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
 * Coupon Query 관련 비즈니스 유스케이스를 처리하는 서비스이다.
 */
@Service
@Transactional(readOnly = true)
public class CouponQueryService {

    private final CouponEventRepository couponEventRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final UserRepository userRepository;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param couponEventRepository 데이터를 조회하고 저장하는 리포지토리
     * @param couponIssueRepository 데이터를 조회하고 저장하는 리포지토리
     * @param userRepository 데이터를 조회하고 저장하는 리포지토리
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
     * 'listActiveEvents' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     * @return 클라이언트에 반환할 API 응답
     */
    public List<CouponEventResponse> listActiveEvents() {
        return couponEventRepository.findAllByStatus(CouponEventStatus.ACTIVE).stream()
            .map(CouponEventResponse::from)
            .toList();
    }

    /**
     * 'getMyCoupons' 메서드는 필요한 데이터를 조회해 호출한 쪽에 반환한다.
     * @param userId 회원 ID
     * @return 클라이언트에 반환할 API 응답
     */
    public List<MyCouponResponse> getMyCoupons(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다."));

        return couponIssueRepository.findAllByUser(user).stream()
            .map(MyCouponResponse::from)
            .toList();
    }
}
