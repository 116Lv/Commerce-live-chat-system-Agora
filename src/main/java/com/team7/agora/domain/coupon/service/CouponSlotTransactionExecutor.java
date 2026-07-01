package com.team7.agora.domain.coupon.service;

import com.team7.agora.domain.coupon.dto.response.CouponEventIssueResponse;
import com.team7.agora.domain.coupon.entity.Coupon;
import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.enums.CouponEventType;
import com.team7.agora.domain.coupon.repository.CouponEventRepository;
import com.team7.agora.domain.coupon.repository.CouponRepository;
import com.team7.agora.domain.coupon.time.CouponEventTime;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CouponSlotTransactionExecutor {

    private final CouponEventRepository couponEventRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    public CouponSlotTransactionExecutor(
        CouponEventRepository couponEventRepository,
        CouponRepository couponRepository,
        UserRepository userRepository
    ) {
        this.couponEventRepository = couponEventRepository;
        this.couponRepository = couponRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CouponEventIssueResponse assignSlotInTransaction(Long eventId, Long userId) {
        CouponEvent event = findEvent(eventId);
        User user = findUser(userId);
        assignSlot(event, user);
        return CouponEventIssueResponse.issued(eventId);
    }

    @Transactional
    public CouponEventIssueResponse assignPublicSlotInTransaction(Long eventId, Long userId) {
        CouponEvent event = findEvent(eventId);
        if (event.getType() == CouponEventType.ADMIN_INDIVIDUAL) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "관리자 전용 쿠폰 이벤트입니다.");
        }
        User user = findUser(userId);
        assignSlot(event, user);
        return CouponEventIssueResponse.issued(eventId);
    }

    @Transactional
    public CouponEventIssueResponse assignSlotsInTransaction(Long eventId, List<Long> userIds) {
        CouponEvent event = findEvent(eventId);
        int issuedCount = 0;
        int skippedCount = 0;

        for (Long userId : userIds.stream().distinct().toList()) {
            User user = findUser(userId);
            if (couponRepository.existsByCouponEventIdAndUserId(eventId, userId)) {
                skippedCount++;
                continue;
            }
            assignSlot(event, user);
            issuedCount++;
        }

        return new CouponEventIssueResponse(eventId, issuedCount, skippedCount);
    }

    private void assignSlot(CouponEvent event, User user) {
        LocalDateTime now = CouponEventTime.now();
        event.validateIssueable(now);
        ensureNotIssuedToUser(event.getId(), user.getId());

        Coupon coupon = couponRepository.findFirstAvailableSlotForUpdate(event.getId())
            .orElseThrow(() -> new BusinessException(ErrorCode.CONFLICT, "쿠폰이 모두 소진되었습니다."));
        coupon.assign(user, now, event.getValidDays());
        event.issue(now);
    }

    private void ensureNotIssuedToUser(Long eventId, Long userId) {
        if (couponRepository.existsByCouponEventIdAndUserId(eventId, userId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 발급받은 쿠폰 이벤트입니다.");
        }
    }

    private CouponEvent findEvent(Long eventId) {
        return couponEventRepository.findById(eventId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "쿠폰 이벤트를 찾을 수 없습니다."));
    }

    private User findUser(Long userId) {
        return userRepository.findByIdAndStatusAndDeletedAtIsNull(userId, UserStatus.ACTIVE)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다."));
    }
}
