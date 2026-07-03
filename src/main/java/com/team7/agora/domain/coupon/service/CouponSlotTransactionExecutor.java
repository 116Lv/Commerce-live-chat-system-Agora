// 호출자가 쿠폰 이벤트별 분산 락을 보유한 상태에서 실행되어야 하는 슬롯 배정 트랜잭션 실행기
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

/**
 * 호출자(CouponSlotService)가 이벤트별 분산 락을 이미 보유한 상태에서만 호출되어야 한다.
 * CouponEvent.issuedQuantity 증가는 @Version이나 행 잠금 없는 단순 read-modify-write라서,
 * 락 없이 동시 호출되면 lost update로 발급 수량이 실제보다 적게 집계될 수 있다.
 * (반면 슬롯 배정 자체는 findFirstAvailableSlotForUpdate 행 잠금과
 * coupons 테이블의 (coupon_event_id, user_id) unique 제약으로 별도 보호된다.)
 */
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

    /**
     * 관리자가 회원 1명을 지정해 발급한다. 이벤트 타입(공개/관리자 전용) 제약을 두지 않는다.
     */
    @Transactional
    public CouponEventIssueResponse assignSlotInTransaction(Long eventId, Long userId) {
        CouponEvent event = findEvent(eventId);
        User user = findUser(userId);
        assignSlot(event, user);
        return CouponEventIssueResponse.issued(eventId);
    }

    /**
     * 사용자가 공개 이벤트에 스스로 참여해 발급받는다. ADMIN_INDIVIDUAL 타입 이벤트는 거부한다.
     */
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

    /**
     * 관리자가 여러 회원에게 일괄 발급한다. 이미 발급받은 회원은 실패시키지 않고 건너뛴다.
     */
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
