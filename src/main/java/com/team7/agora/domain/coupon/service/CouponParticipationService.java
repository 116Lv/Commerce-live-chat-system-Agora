package com.team7.agora.domain.coupon.service;

import com.team7.agora.domain.coupon.dto.response.CouponEventIssueResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CouponParticipationService {

    private final CouponSlotService couponSlotService;

    public CouponParticipationService(CouponSlotService couponSlotService) {
        this.couponSlotService = couponSlotService;
    }

    public CouponEventIssueResponse participate(Long userId, Long eventId) {
        return couponSlotService.assignPublicSlot(eventId, userId);
    }
}
