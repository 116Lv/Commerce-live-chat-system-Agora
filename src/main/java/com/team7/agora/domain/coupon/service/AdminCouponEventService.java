package com.team7.agora.domain.coupon.service;

import com.team7.agora.domain.coupon.dto.response.AdminCouponEventResponse;
import com.team7.agora.domain.coupon.dto.response.CouponEventCouponResponse;
import com.team7.agora.domain.coupon.dto.response.CouponEventIssueResponse;
import com.team7.agora.domain.coupon.entity.Coupon;
import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.enums.CouponEventType;
import com.team7.agora.domain.coupon.repository.CouponEventRepository;
import com.team7.agora.domain.coupon.repository.CouponRepository;
import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminCouponEventService {

    private final CouponEventRepository couponEventRepository;
    private final CouponRepository couponRepository;
    private final CouponSlotService couponSlotService;

    public AdminCouponEventService(
        CouponEventRepository couponEventRepository,
        CouponRepository couponRepository,
        CouponSlotService couponSlotService
    ) {
        this.couponEventRepository = couponEventRepository;
        this.couponRepository = couponRepository;
        this.couponSlotService = couponSlotService;
    }

    @Transactional
    public AdminCouponEventResponse createEvent(
        CustomUserDetails admin,
        CouponEventType type,
        String name,
        LocalDateTime startAt,
        LocalDateTime endAt,
        int totalQuantity,
        int discountAmount,
        int minOrderAmount,
        int validDays
    ) {
        validateAdminAuthority(admin);
        validateEventWindow(startAt, endAt);

        CouponEvent event = couponEventRepository.save(
            CouponEvent.create(type, name, totalQuantity, startAt, endAt, discountAmount, minOrderAmount, validDays)
        );
        List<Coupon> slots = IntStream.range(0, totalQuantity)
            .mapToObj(ignored -> Coupon.createAvailableSlot(event))
            .toList();
        couponRepository.saveAll(slots);

        return AdminCouponEventResponse.from(event);
    }

    public List<AdminCouponEventResponse> getList(CustomUserDetails admin) {
        validateAdminAuthority(admin);
        return couponEventRepository.findAll().stream()
            .map(AdminCouponEventResponse::from)
            .toList();
    }

    public AdminCouponEventResponse getDetail(CustomUserDetails admin, Long eventId) {
        validateAdminAuthority(admin);
        return AdminCouponEventResponse.from(findEvent(eventId));
    }

    public CouponEventIssueResponse issueToUsers(CustomUserDetails admin, Long eventId, List<Long> userIds) {
        validateAdminAuthority(admin);
        CouponEvent event = findEvent(eventId);
        if (event.getType() != CouponEventType.ADMIN_INDIVIDUAL) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "관리자 개별 발급 이벤트만 지정 발급할 수 있습니다.");
        }
        return couponSlotService.assignSlots(eventId, userIds);
    }

    public List<CouponEventCouponResponse> getCoupons(CustomUserDetails admin, Long eventId) {
        validateAdminAuthority(admin);
        findEvent(eventId);
        return couponRepository.findAllByCouponEventIdAndUserIsNotNull(eventId).stream()
            .map(CouponEventCouponResponse::from)
            .toList();
    }

    private CouponEvent findEvent(Long eventId) {
        return couponEventRepository.findById(eventId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "쿠폰 이벤트를 찾을 수 없습니다."));
    }

    private void validateEventWindow(LocalDateTime startAt, LocalDateTime endAt) {
        if (!startAt.isBefore(endAt)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "이벤트 시작 시각은 종료 시각보다 빨라야 합니다.");
        }
    }

    private void validateAdminAuthority(CustomUserDetails admin) {
        if (admin == null || !(admin.getRole() == UserRole.ROOT_ADMIN || admin.getRole() == UserRole.USER_ADMIN)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "쿠폰 이벤트 관리는 관리자만 수행할 수 있습니다.");
        }
    }
}
