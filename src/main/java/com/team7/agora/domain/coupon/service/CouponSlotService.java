// 쿠폰 이벤트별 분산 락을 잡은 뒤 슬롯 배정 트랜잭션에 위임하는 진입점
package com.team7.agora.domain.coupon.service;

import com.team7.agora.domain.coupon.dto.response.CouponEventIssueResponse;
import com.team7.agora.global.lock.LockService;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 이벤트 ID 기준 분산 락(lock:coupon-event:{eventId})으로 같은 이벤트에 대한 발급 요청을
 * 직렬화한 뒤 {@link CouponSlotTransactionExecutor}에 실제 배정을 위임한다.
 * 이 락은 CouponEvent.issuedQuantity 증가의 lost update를 막기 위한 것이라
 * CouponSlotTransactionExecutor를 락 밖에서 직접 호출하면 안 된다.
 */
@Service
public class CouponSlotService {

    private static final String COUPON_EVENT_LOCK_PREFIX = "lock:coupon-event:";

    private final LockService lockService;
    private final CouponSlotTransactionExecutor executor;

    public CouponSlotService(LockService lockService, CouponSlotTransactionExecutor executor) {
        this.lockService = lockService;
        this.executor = executor;
    }

    public CouponEventIssueResponse assignSlot(Long eventId, Long userId) {
        return lockService.withLock(
            COUPON_EVENT_LOCK_PREFIX + eventId,
            () -> executor.assignSlotInTransaction(eventId, userId)
        );
    }

    public CouponEventIssueResponse assignPublicSlot(Long eventId, Long userId) {
        return lockService.withLock(
            COUPON_EVENT_LOCK_PREFIX + eventId,
            () -> executor.assignPublicSlotInTransaction(eventId, userId)
        );
    }

    public CouponEventIssueResponse assignSlots(Long eventId, List<Long> userIds) {
        return lockService.withLock(
            COUPON_EVENT_LOCK_PREFIX + eventId,
            () -> executor.assignSlotsInTransaction(eventId, userIds)
        );
    }
}
