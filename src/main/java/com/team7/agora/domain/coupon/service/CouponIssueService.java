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
import com.team7.agora.global.lock.LockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service that coordinates coupon issue use cases.
 */
@Service
@Transactional(readOnly = true)
public class CouponIssueService {

    private final CouponEventRepository couponEventRepository;
    private final CouponRepository couponRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final UserRepository userRepository;
    private final LockService lockService;

    /**
     * Creates a coupon issue service instance.
     * @param couponEventRepository the coupon event repository value
     * @param couponRepository the coupon repository value
     * @param couponIssueRepository the coupon issue repository value
     * @param userRepository the user repository value
     * @param lockService the lock service value
     */
    public CouponIssueService(
        CouponEventRepository couponEventRepository,
        CouponRepository couponRepository,
        CouponIssueRepository couponIssueRepository,
        UserRepository userRepository,
        LockService lockService
    ) {
        this.couponEventRepository = couponEventRepository;
        this.couponRepository = couponRepository;
        this.couponIssueRepository = couponIssueRepository;
        this.userRepository = userRepository;
        this.lockService = lockService;
    }

    /**
     * Checks whether issue applies.
     * @param userId the user id value
     * @param couponEventId the coupon event id value
     */
    @Transactional
    public void issue(Long userId, Long couponEventId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다."));

        lockService.withLock("lock:coupon-event:" + couponEventId, () -> {
            CouponEvent couponEvent = couponEventRepository.findById(couponEventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "쿠폰 이벤트를 찾을 수 없습니다."));

            couponEvent.validateIssueable(java.time.LocalDateTime.now());

            if (couponIssueRepository.existsByCouponEventAndUser(couponEvent, user)) {
                throw new BusinessException(ErrorCode.CONFLICT, "이미 발급받은 쿠폰입니다.");
            }

            Coupon coupon = couponRepository.findFirstComeCoupon()
                .orElseGet(() -> couponRepository.save(Coupon.firstCome("선착순 할인 쿠폰", 5000, 10000, 30)));

            couponEvent.issue();
            couponIssueRepository.save(CouponIssue.issue(coupon, couponEvent, user));
            return null;
        });
    }
}
