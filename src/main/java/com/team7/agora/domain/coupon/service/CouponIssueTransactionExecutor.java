// 분산 락 안에서 실행되어, 트랜잭션 커밋까지 락 보호 범위에 포함시키는 실행기
package com.team7.agora.domain.coupon.service;

import com.team7.agora.domain.coupon.entity.Coupon;
import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.entity.CouponIssue;
import com.team7.agora.domain.coupon.repository.CouponEventRepository;
import com.team7.agora.domain.coupon.repository.CouponIssueRepository;
import com.team7.agora.domain.coupon.repository.CouponRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CouponIssueTransactionExecutor {

    private final CouponEventRepository couponEventRepository;
    private final CouponRepository couponRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final UserRepository userRepository;

    public CouponIssueTransactionExecutor(
        CouponEventRepository couponEventRepository,
        CouponRepository couponRepository,
        CouponIssueRepository couponIssueRepository,
        UserRepository userRepository
    ) {
        this.couponEventRepository = couponEventRepository;
        this.couponRepository = couponRepository;
        this.couponIssueRepository = couponIssueRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void issue(Long userId, Long couponEventId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다."));
        CouponEvent couponEvent = couponEventRepository.findById(couponEventId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "쿠폰 이벤트를 찾을 수 없습니다."));

        couponEvent.validateIssueable(LocalDateTime.now());

        if (couponIssueRepository.existsByCouponEventAndUser(couponEvent, user)) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 발급받은 쿠폰입니다.");
        }

        Coupon coupon = couponRepository.findFirstComeCoupon()
            .orElseGet(() -> couponRepository.save(Coupon.firstCome("선착순 할인 쿠폰", 5000, 10000, 30)));

        couponEvent.issue();
        couponIssueRepository.save(CouponIssue.issue(coupon, couponEvent, user));
    }
}
