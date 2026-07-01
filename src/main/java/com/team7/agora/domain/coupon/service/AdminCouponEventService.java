package com.team7.agora.domain.coupon.service;

import com.team7.agora.domain.admin.dto.response.AdminApprovalRequestResponse;
import com.team7.agora.domain.admin.entity.Admin;
import com.team7.agora.domain.admin.entity.AdminApprovalRequest;
import com.team7.agora.domain.admin.enums.AdminApprovalOperation;
import com.team7.agora.domain.admin.enums.AdminPermission;
import com.team7.agora.domain.admin.enums.AdminRole;
import com.team7.agora.domain.admin.repository.AdminApprovalRequestRepository;
import com.team7.agora.domain.admin.repository.AdminRepository;
import com.team7.agora.domain.admin.service.AdminRoleSupport;
import com.team7.agora.domain.coupon.dto.response.AdminCouponEventResponse;
import com.team7.agora.domain.coupon.dto.response.AdminCouponIssueApprovalResponse;
import com.team7.agora.domain.coupon.dto.response.CouponEventCouponResponse;
import com.team7.agora.domain.coupon.entity.AdminCouponApprovalPayload;
import com.team7.agora.domain.coupon.entity.Coupon;
import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.enums.CouponEventStatus;
import com.team7.agora.domain.coupon.enums.CouponEventType;
import com.team7.agora.domain.coupon.repository.AdminCouponApprovalPayloadRepository;
import com.team7.agora.domain.coupon.repository.CouponEventRepository;
import com.team7.agora.domain.coupon.repository.CouponRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import com.team7.agora.global.time.AgoraClock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminCouponEventService {

    private static final int MAX_ADMIN_ISSUE_USER_COUNT = 100;
    private static final List<CouponEventStatus> DEFAULT_ADMIN_LIST_STATUSES = List.of(
        CouponEventStatus.ACTIVE,
        CouponEventStatus.STOP_REQUESTED,
        CouponEventStatus.STOPPED,
        CouponEventStatus.ENDED
    );

    private final CouponEventRepository couponEventRepository;
    private final CouponRepository couponRepository;
    private final CouponSlotService couponSlotService;
    private final AdminRepository adminRepository;
    private final AdminApprovalRequestRepository approvalRequestRepository;
    private final AdminCouponApprovalPayloadRepository approvalPayloadRepository;
    private final UserRepository userRepository;

    public AdminCouponEventService(
        CouponEventRepository couponEventRepository,
        CouponRepository couponRepository,
        CouponSlotService couponSlotService,
        AdminRepository adminRepository,
        AdminApprovalRequestRepository approvalRequestRepository,
        AdminCouponApprovalPayloadRepository approvalPayloadRepository,
        UserRepository userRepository
    ) {
        this.couponEventRepository = couponEventRepository;
        this.couponRepository = couponRepository;
        this.couponSlotService = couponSlotService;
        this.adminRepository = adminRepository;
        this.approvalRequestRepository = approvalRequestRepository;
        this.approvalPayloadRepository = approvalPayloadRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AdminApprovalRequestResponse createEvent(
        AdminPrincipal admin,
        CouponEventType type,
        String name,
        LocalDateTime startAt,
        LocalDateTime endAt,
        int totalQuantity,
        int discountAmount,
        int minOrderAmount,
        int validDays,
        String reason
    ) {
        validateAdminAuthority(admin);
        validateCreateFields(type, name, totalQuantity, discountAmount, minOrderAmount, validDays);
        validateEventWindow(startAt, endAt);
        validateCreateReason(reason);

        Admin requester = getCurrentAdmin(admin);
        CouponEvent event = couponEventRepository.save(
            CouponEvent.createPending(type, name, totalQuantity, startAt, endAt, discountAmount, minOrderAmount, validDays)
        );
        AdminApprovalRequest request = saveCouponApprovalRequest(AdminApprovalRequest.createCouponOperation(
            AdminApprovalOperation.COUPON_EVENT_CREATE,
            requester,
            reason,
            "COUPON_EVENT_CREATE:" + event.getId()
        ));
        AdminCouponApprovalPayload payload = approvalPayloadRepository.save(AdminCouponApprovalPayload.forCreate(request, event));

        if (admin.getRole() == AdminRole.ROOT_ADMIN) {
            request.approve(requester, "Auto-approved by ROOT_ADMIN");
            activateEventAndCreateSlots(event);
        }

        return AdminApprovalRequestResponse.from(request, payload);
    }

    public List<AdminCouponEventResponse> getList(AdminPrincipal admin, CouponEventStatus status) {
        validateAdminAuthority(admin);
        List<CouponEvent> events = status == null
            ? couponEventRepository.findAllByStatusIn(DEFAULT_ADMIN_LIST_STATUSES)
            : couponEventRepository.findAllByStatus(status);

        return events.stream()
            .map(AdminCouponEventResponse::from)
            .toList();
    }

    public AdminCouponEventResponse getDetail(AdminPrincipal admin, Long eventId) {
        validateAdminAuthority(admin);
        return AdminCouponEventResponse.from(findEvent(eventId));
    }

    @Transactional
    public AdminApprovalRequestResponse requestStop(AdminPrincipal admin, Long eventId, String reason) {
        validateAdminAuthority(admin);
        Admin requester = getCurrentAdmin(admin);
        CouponEvent event = findEvent(eventId);
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Stop reason is required.");
        }
        if (event.getStatus() != CouponEventStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.CONFLICT, "Only ACTIVE coupon events can request stop.");
        }

        String pendingRequestKey = "COUPON_EVENT_STOP:" + event.getId();
        validateNoPendingRequest(pendingRequestKey);

        AdminApprovalRequest request = saveCouponApprovalRequest(AdminApprovalRequest.createCouponOperation(
            AdminApprovalOperation.COUPON_EVENT_STOP,
            requester,
            reason,
            pendingRequestKey
        ));
        AdminCouponApprovalPayload payload = approvalPayloadRepository.save(
            AdminCouponApprovalPayload.forStop(request, event.getId())
        );

        if (admin.getRole() == AdminRole.ROOT_ADMIN) {
            event.requestStop();
            event.stop();
            request.approve(requester, "Auto-approved by ROOT_ADMIN");
        }

        return AdminApprovalRequestResponse.from(request, payload);
    }

    @Transactional
    public AdminCouponIssueApprovalResponse requestIssueToUsers(AdminPrincipal admin, Long eventId, List<?> issueTargets) {
        validateAdminAuthority(admin);
        validateIssueTargetsForApproval(issueTargets);
        Admin requester = getCurrentAdmin(admin);
        CouponEvent event = findEvent(eventId);
        if (event.getType() != CouponEventType.ADMIN_INDIVIDUAL) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Only ADMIN_INDIVIDUAL events support admin issue requests.");
        }
        event.validateIssueable(AgoraClock.now());

        String pendingRequestKey = "COUPON_EVENT_ISSUE:" + event.getId() + ":" + requester.getId();
        validateNoPendingRequest(pendingRequestKey);

        IssueSummary summary = summarizeIssueTargets(event, issueTargets);
        AdminApprovalRequest request = saveCouponApprovalRequest(AdminApprovalRequest.createCouponOperation(
            AdminApprovalOperation.COUPON_EVENT_ISSUE,
            requester,
            "Coupon event issue: " + event.getName(),
            pendingRequestKey
        ));
        AdminCouponApprovalPayload payload = approvalPayloadRepository.save(AdminCouponApprovalPayload.forIssue(
            request,
            event.getId(),
            summary.validTargetIds(),
            summary.inputCount(),
            summary.validTargetCount(),
            summary.duplicateCount(),
            summary.excludedCount(),
            summary.plannedIssueCount(),
            summary.expectedIssuedQuantity(),
            summary.exceedsRemainingQuantity()
        ));

        if (admin.getRole() == AdminRole.ROOT_ADMIN) {
            request.approve(requester, "Auto-approved by ROOT_ADMIN");
            couponSlotService.assignSlots(event.getId(), payload.targetUserIdList());
        }

        return AdminCouponIssueApprovalResponse.from(payload);
    }

    public List<CouponEventCouponResponse> getCoupons(AdminPrincipal admin, Long eventId) {
        validateAdminAuthority(admin);
        findEvent(eventId);
        return couponRepository.findAllByCouponEventIdAndUserIsNotNull(eventId).stream()
            .map(CouponEventCouponResponse::from)
            .toList();
    }

    private CouponEvent findEvent(Long eventId) {
        return couponEventRepository.findById(eventId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Coupon event not found."));
    }

    private void validateCreateFields(
        CouponEventType type,
        String name,
        int totalQuantity,
        int discountAmount,
        int minOrderAmount,
        int validDays
    ) {
        if (type == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Coupon event type is required.");
        }
        if (name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Coupon event name is required.");
        }
        if (totalQuantity <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Total quantity must be positive.");
        }
        if (discountAmount <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Discount amount must be positive.");
        }
        if (minOrderAmount < 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Minimum order amount cannot be negative.");
        }
        if (validDays <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Valid days must be positive.");
        }
    }

    private void validateEventWindow(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt == null || endAt == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Coupon event start and end time are required.");
        }
        if (!startAt.isBefore(endAt)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Coupon event start time must be before end time.");
        }
    }

    private void validateCreateReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Coupon event approval reason is required.");
        }
    }

    private void validateIssueTargetsForApproval(List<?> issueTargets) {
        if (issueTargets == null || issueTargets.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "At least one issue target user is required.");
        }
        if (issueTargets.size() > MAX_ADMIN_ISSUE_USER_COUNT) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Admin individual issue supports up to 100 users.");
        }
    }

    private void validateNoPendingRequest(String pendingRequestKey) {
        if (approvalRequestRepository.existsByPendingRequestKey(pendingRequestKey)) {
            throw new BusinessException(ErrorCode.CONFLICT, "A pending approval request already exists.");
        }
    }

    private AdminApprovalRequest saveCouponApprovalRequest(AdminApprovalRequest request) {
        try {
            return approvalRequestRepository.save(request);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException(ErrorCode.CONFLICT, "A pending approval request already exists.");
        }
    }

    private void validateAdminAuthority(AdminPrincipal admin) {
        if (!AdminRoleSupport.hasPermission(admin, AdminPermission.COUPON_MANAGE)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Only coupon managers can manage coupon events.");
        }
    }

    private Admin getCurrentAdmin(AdminPrincipal admin) {
        if (admin == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return adminRepository.findById(admin.getAdminId())
            .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Admin account not found."));
    }

    private void activateEventAndCreateSlots(CouponEvent event) {
        event.approve();
        List<Coupon> slots = IntStream.range(0, event.getTotalQuantity())
            .mapToObj(ignored -> Coupon.createAvailableSlot(event))
            .toList();
        couponRepository.saveAll(slots);
    }

    private IssueSummary summarizeIssueTargets(CouponEvent event, List<?> issueTargets) {
        Set<Long> seen = new LinkedHashSet<>();
        List<Long> eligibleTargetIds = new ArrayList<>();
        int duplicates = 0;
        int excluded = 0;
        for (Object issueTarget : issueTargets) {
            Long userId = resolveIssueTargetUserId(issueTarget);
            if (userId == null) {
                excluded++;
                continue;
            }
            if (!seen.add(userId)) {
                duplicates++;
                continue;
            }
            if (userRepository.findByIdAndStatusAndDeletedAtIsNull(userId, UserStatus.ACTIVE).isEmpty()) {
                excluded++;
                continue;
            }
            if (couponRepository.existsByCouponEventIdAndUserId(event.getId(), userId)) {
                excluded++;
                continue;
            }
            eligibleTargetIds.add(userId);
        }
        int remaining = event.getTotalQuantity() - event.getIssuedQuantity();
        List<Long> executionTargetIds = eligibleTargetIds.stream()
            .limit(Math.max(remaining, 0))
            .toList();
        return new IssueSummary(
            issueTargets.size(),
            eligibleTargetIds.size(),
            executionTargetIds,
            duplicates,
            excluded,
            executionTargetIds.size(),
            event.getIssuedQuantity() + executionTargetIds.size(),
            eligibleTargetIds.size() > remaining
        );
    }

    private Long resolveIssueTargetUserId(Object issueTarget) {
        String token = String.valueOf(issueTarget == null ? "" : issueTarget).trim();
        if (token.isEmpty()) {
            return null;
        }
        if (token.matches("\\d+")) {
            try {
                return Long.valueOf(token);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        List<User> matches = userRepository.findAllByNicknameAndStatusAndDeletedAtIsNull(token, UserStatus.ACTIVE);
        if (matches.size() != 1) {
            return null;
        }
        return matches.get(0).getId();
    }

    private record IssueSummary(
        int inputCount,
        int validTargetCount,
        List<Long> validTargetIds,
        int duplicateCount,
        int excludedCount,
        int plannedIssueCount,
        int expectedIssuedQuantity,
        boolean exceedsRemainingQuantity
    ) {
    }
}
