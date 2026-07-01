package com.team7.agora.domain.admin.service;

import com.team7.agora.domain.admin.dto.response.AdminApprovalRequestResponse;
import com.team7.agora.domain.admin.entity.Admin;
import com.team7.agora.domain.admin.entity.AdminApprovalRequest;
import com.team7.agora.domain.admin.enums.AdminApprovalOperation;
import com.team7.agora.domain.admin.enums.AdminApprovalStatus;
import com.team7.agora.domain.admin.enums.AdminRole;
import com.team7.agora.domain.admin.repository.AdminApprovalRequestRepository;
import com.team7.agora.domain.admin.repository.AdminRepository;
import com.team7.agora.domain.coupon.entity.AdminCouponApprovalPayload;
import com.team7.agora.domain.coupon.entity.Coupon;
import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.enums.CouponEventType;
import com.team7.agora.domain.coupon.repository.AdminCouponApprovalPayloadRepository;
import com.team7.agora.domain.coupon.repository.CouponEventRepository;
import com.team7.agora.domain.coupon.repository.CouponRepository;
import com.team7.agora.domain.coupon.service.CouponSlotService;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminApprovalService {

    private final AdminApprovalRequestRepository approvalRequestRepository;
    private final AdminRepository adminRepository;
    private final AdminCouponApprovalPayloadRepository approvalPayloadRepository;
    private final CouponEventRepository couponEventRepository;
    private final CouponRepository couponRepository;
    private final CouponSlotService couponSlotService;

    public AdminApprovalService(
            AdminApprovalRequestRepository approvalRequestRepository,
            AdminRepository adminRepository,
            AdminCouponApprovalPayloadRepository approvalPayloadRepository,
            CouponEventRepository couponEventRepository,
            CouponRepository couponRepository,
            CouponSlotService couponSlotService
    ) {
        this.approvalRequestRepository = approvalRequestRepository;
        this.adminRepository = adminRepository;
        this.approvalPayloadRepository = approvalPayloadRepository;
        this.couponEventRepository = couponEventRepository;
        this.couponRepository = couponRepository;
        this.couponSlotService = couponSlotService;
    }

    public List<AdminApprovalRequestResponse> getRequests(AdminPrincipal admin, String status) {
        validateRootAdmin(admin);
        if (status == null || status.isBlank()) {
            return approvalRequestRepository.findAllByOrderByCreatedAtDesc().stream()
                    .map(this::toResponse)
                    .toList();
        }

        AdminApprovalStatus approvalStatus = parseStatus(status);
        return approvalRequestRepository.findAllByStatusOrderByCreatedAtDesc(approvalStatus).stream()
                .map(this::toResponse)
                .toList();
    }

    public AdminApprovalRequestResponse getRequestDetail(AdminPrincipal admin, Long requestId) {
        validateRootAdmin(admin);
        return toResponse(approvalRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Approval request not found.")));
    }

    public List<AdminApprovalRequestResponse> getMyRequests(AdminPrincipal admin) {
        Admin requester = getCurrentAdmin(admin);
        return approvalRequestRepository.findAllByRequesterOrderByCreatedAtDesc(requester).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AdminApprovalRequestResponse requestRoleChange(
            AdminPrincipal admin,
            Long targetAdminId,
            AdminRole requestedRole,
            String reason
    ) {
        Admin requester = getCurrentAdmin(admin);
        Admin target = adminRepository.findById(targetAdminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Target admin account not found."));
        if (target.getRole() == requestedRole) {
            throw new BusinessException(ErrorCode.CONFLICT, "Target admin already has the requested role.");
        }
        if (approvalRequestRepository.existsByRequesterAndTargetAdminAndRequestedRoleAndStatus(
                requester,
                target,
                requestedRole,
                AdminApprovalStatus.PENDING
        )) {
            throw new BusinessException(ErrorCode.CONFLICT, "A pending approval request already exists.");
        }

        AdminApprovalRequest request = AdminApprovalRequest.createRoleChange(requester, target, requestedRole, reason);
        try {
            return AdminApprovalRequestResponse.from(approvalRequestRepository.save(request));
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException(ErrorCode.CONFLICT, "A pending approval request already exists.");
        }
    }

    @Transactional
    public AdminApprovalRequestResponse approve(AdminPrincipal admin, Long requestId, String memo) {
        Admin approver = getRootAdmin(admin);
        AdminApprovalRequest request = getRequest(requestId);
        AdminCouponApprovalPayload payload = findPayload(request).orElse(null);
        request.assertPending();
        executeApproval(admin, request, payload);
        request.approve(approver, memo);
        return AdminApprovalRequestResponse.from(request, payload);
    }

    @Transactional
    public AdminApprovalRequestResponse reject(AdminPrincipal admin, Long requestId, String memo) {
        Admin approver = getRootAdmin(admin);
        AdminApprovalRequest request = getRequest(requestId);
        AdminCouponApprovalPayload payload = findPayload(request).orElse(null);
        request.assertPending();
        executeRejection(request, payload);
        request.reject(approver, memo);
        return AdminApprovalRequestResponse.from(request, payload);
    }

    private void executeApproval(
            AdminPrincipal admin,
            AdminApprovalRequest request,
            AdminCouponApprovalPayload payload
    ) {
        switch (request.getOperation()) {
            case ADMIN_ROLE_CHANGE -> {
                validateRoleChange(admin, request.getTargetAdmin(), request.getRequestedRole());
                request.applyRoleChange();
            }
            case COUPON_EVENT_CREATE -> {
                AdminCouponApprovalPayload requiredPayload = requirePayload(payload);
                CouponEvent event = findCouponEvent(requiredPayload.getCouponEventId());
                event.approve();
                createSlots(event);
            }
            case COUPON_EVENT_STOP -> {
                CouponEvent event = findCouponEvent(requirePayload(payload).getCouponEventId());
                event.requestStop();
                event.stop();
            }
            case COUPON_EVENT_ISSUE -> {
                AdminCouponApprovalPayload requiredPayload = requirePayload(payload);
                CouponEvent event = findCouponEvent(requiredPayload.getCouponEventId());
                if (event.getType() != CouponEventType.ADMIN_INDIVIDUAL) {
                    throw new BusinessException(ErrorCode.INVALID_REQUEST, "Only ADMIN_INDIVIDUAL events support admin issue.");
                }
                couponSlotService.assignSlots(event.getId(), requiredPayload.targetUserIdList());
            }
        }
    }

    private void executeRejection(AdminApprovalRequest request, AdminCouponApprovalPayload payload) {
        if (request.getOperation() == AdminApprovalOperation.COUPON_EVENT_CREATE) {
            findCouponEvent(requirePayload(payload).getCouponEventId()).reject();
        }
    }

    private AdminCouponApprovalPayload requirePayload(AdminCouponApprovalPayload payload) {
        if (payload == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Coupon approval payload not found.");
        }
        return payload;
    }

    private Optional<AdminCouponApprovalPayload> findPayload(AdminApprovalRequest request) {
        if (request.getOperation() == AdminApprovalOperation.ADMIN_ROLE_CHANGE) {
            return Optional.empty();
        }
        return approvalPayloadRepository.findByApprovalRequest(request);
    }

    private CouponEvent findCouponEvent(Long eventId) {
        return couponEventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Coupon event not found."));
    }

    private void createSlots(CouponEvent event) {
        List<Coupon> slots = IntStream.range(0, event.getTotalQuantity())
                .mapToObj(ignored -> Coupon.createAvailableSlot(event))
                .toList();
        couponRepository.saveAll(slots);
    }

    private AdminApprovalRequestResponse toResponse(AdminApprovalRequest request) {
        return AdminApprovalRequestResponse.from(request, findPayload(request).orElse(null));
    }

    private AdminApprovalRequest getRequest(Long requestId) {
        return approvalRequestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Approval request not found."));
    }

    private Admin getCurrentAdmin(AdminPrincipal admin) {
        if (admin == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return adminRepository.findById(admin.getAdminId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Admin account not found."));
    }

    private Admin getRootAdmin(AdminPrincipal admin) {
        validateRootAdmin(admin);
        return adminRepository.findById(admin.getAdminId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Admin account not found."));
    }

    private void validateRootAdmin(AdminPrincipal admin) {
        if (admin == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (admin.getRole() != AdminRole.ROOT_ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Only ROOT_ADMIN can approve requests.");
        }
    }

    private void validateRoleChange(AdminPrincipal admin, Admin target, AdminRole role) {
        if (target == null || role == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (target.getRole() == AdminRole.ROOT_ADMIN && role != AdminRole.ROOT_ADMIN) {
            if (target.getId().equals(admin.getAdminId())) {
                throw new BusinessException(ErrorCode.CONFLICT, "ROOT_ADMIN cannot demote their own account.");
            }

            adminRepository.findAllByRoleForUpdate(AdminRole.ROOT_ADMIN);
            if (adminRepository.countByRole(AdminRole.ROOT_ADMIN) <= 1) {
                throw new BusinessException(ErrorCode.CONFLICT, "At least one ROOT_ADMIN account must remain.");
            }
        }
    }

    private AdminApprovalStatus parseStatus(String status) {
        try {
            return AdminApprovalStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Invalid approval status.");
        }
    }
}
