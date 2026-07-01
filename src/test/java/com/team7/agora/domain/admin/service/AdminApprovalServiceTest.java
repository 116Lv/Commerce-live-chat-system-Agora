package com.team7.agora.domain.admin.service;

import static com.team7.agora.support.TestEntityIds.assignId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.admin.dto.response.AdminApprovalRequestResponse;
import com.team7.agora.domain.admin.entity.Admin;
import com.team7.agora.domain.admin.entity.AdminApprovalRequest;
import com.team7.agora.domain.admin.enums.AdminApprovalOperation;
import com.team7.agora.domain.admin.enums.AdminApprovalStatus;
import com.team7.agora.domain.admin.enums.AdminRole;
import com.team7.agora.domain.admin.enums.AdminStatus;
import com.team7.agora.domain.admin.repository.AdminApprovalRequestRepository;
import com.team7.agora.domain.admin.repository.AdminRepository;
import com.team7.agora.domain.coupon.entity.AdminCouponApprovalPayload;
import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.enums.CouponEventType;
import com.team7.agora.domain.coupon.repository.AdminCouponApprovalPayloadRepository;
import com.team7.agora.domain.coupon.repository.CouponEventRepository;
import com.team7.agora.domain.coupon.repository.CouponRepository;
import com.team7.agora.domain.coupon.service.CouponSlotService;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class AdminApprovalServiceTest {

    @Mock
    private AdminApprovalRequestRepository approvalRequestRepository;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private AdminCouponApprovalPayloadRepository approvalPayloadRepository;

    @Mock
    private CouponEventRepository couponEventRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponSlotService couponSlotService;

    private AdminApprovalService createService() {
        return new AdminApprovalService(
                approvalRequestRepository,
                adminRepository,
                approvalPayloadRepository,
                couponEventRepository,
                couponRepository,
                couponSlotService
        );
    }

    @Test
    void requestRoleChange_createsPendingRequestForDomainAdmin() {
        AdminApprovalService service = createService();
        Admin requester = admin(2L, "user-admin@test.com", "user-admin", AdminRole.USER_ADMIN);
        Admin target = admin(3L, "product-admin@test.com", "product-admin", AdminRole.PRODUCT_ADMIN);
        when(adminRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(adminRepository.findById(3L)).thenReturn(Optional.of(target));
        when(approvalRequestRepository.save(org.mockito.ArgumentMatchers.any(AdminApprovalRequest.class)))
                .thenAnswer(invocation -> {
                    AdminApprovalRequest request = invocation.getArgument(0);
                    assignId(request, 10L);
                    return request;
                });

        AdminApprovalRequestResponse response = service.requestRoleChange(
                principal(2L, AdminRole.USER_ADMIN),
                3L,
                AdminRole.SETTLEMENT_ADMIN,
                "Temporary settlement backup"
        );

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.operation()).isEqualTo("ADMIN_ROLE_CHANGE");
        assertThat(response.requesterNickname()).isEqualTo("user-admin");
        assertThat(response.targetAdminNickname()).isEqualTo("product-admin");
        assertThat(response.requestedRole()).isEqualTo("SETTLEMENT_ADMIN");
    }

    @Test
    void requestRoleChange_allowsRootToCreateApprovalRequestInsteadOfDirectPatch() {
        AdminApprovalService service = createService();
        Admin requester = admin(1L, "root@test.com", "root", AdminRole.ROOT_ADMIN);
        Admin target = admin(3L, "product-admin@test.com", "product-admin", AdminRole.PRODUCT_ADMIN);
        when(adminRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(adminRepository.findById(3L)).thenReturn(Optional.of(target));
        when(approvalRequestRepository.save(org.mockito.ArgumentMatchers.any(AdminApprovalRequest.class)))
                .thenAnswer(invocation -> {
                    AdminApprovalRequest request = invocation.getArgument(0);
                    assignId(request, 11L);
                    return request;
                });

        AdminApprovalRequestResponse response = service.requestRoleChange(
                principal(1L, AdminRole.ROOT_ADMIN),
                3L,
                AdminRole.SETTLEMENT_ADMIN,
                "Root submitted high-risk role change for review"
        );

        assertThat(response.id()).isEqualTo(11L);
        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(target.getRole()).isEqualTo(AdminRole.PRODUCT_ADMIN);
    }

    @Test
    void requestRoleChange_rejectsDuplicatePendingRequestForSameRequesterTargetAndRole() {
        AdminApprovalService service = createService();
        Admin requester = admin(2L, "user-admin@test.com", "user-admin", AdminRole.USER_ADMIN);
        Admin target = admin(3L, "product-admin@test.com", "product-admin", AdminRole.PRODUCT_ADMIN);
        when(adminRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(adminRepository.findById(3L)).thenReturn(Optional.of(target));
        when(approvalRequestRepository.existsByRequesterAndTargetAdminAndRequestedRoleAndStatus(
                requester,
                target,
                AdminRole.SETTLEMENT_ADMIN,
                AdminApprovalStatus.PENDING
        )).thenReturn(true);

        assertThatThrownBy(() -> service.requestRoleChange(
                principal(2L, AdminRole.USER_ADMIN),
                3L,
                AdminRole.SETTLEMENT_ADMIN,
                "Duplicate"
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONFLICT);
    }

    @Test
    void requestRoleChange_translatesPendingRequestUniqueRaceToConflict() {
        AdminApprovalService service = createService();
        Admin requester = admin(2L, "user-admin@test.com", "user-admin", AdminRole.USER_ADMIN);
        Admin target = admin(3L, "product-admin@test.com", "product-admin", AdminRole.PRODUCT_ADMIN);
        when(adminRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(adminRepository.findById(3L)).thenReturn(Optional.of(target));
        when(approvalRequestRepository.save(org.mockito.ArgumentMatchers.any(AdminApprovalRequest.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate pending request"));

        assertThatThrownBy(() -> service.requestRoleChange(
                principal(2L, AdminRole.USER_ADMIN),
                3L,
                AdminRole.SETTLEMENT_ADMIN,
                "Race duplicate"
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONFLICT);
    }

    @Test
    void getMyRequests_returnsOnlyRequesterRequestsForDomainAdmin() {
        AdminApprovalService service = createService();
        Admin requester = admin(2L, "user-admin@test.com", "user-admin", AdminRole.USER_ADMIN);
        Admin target = admin(3L, "product-admin@test.com", "product-admin", AdminRole.PRODUCT_ADMIN);
        AdminApprovalRequest request = AdminApprovalRequest.createRoleChange(
                requester,
                target,
                AdminRole.SETTLEMENT_ADMIN,
                "Temporary settlement backup"
        );
        assignId(request, 10L);
        when(adminRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(approvalRequestRepository.findAllByRequesterOrderByCreatedAtDesc(requester)).thenReturn(List.of(request));

        List<AdminApprovalRequestResponse> responses = service.getMyRequests(principal(2L, AdminRole.USER_ADMIN));

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).id()).isEqualTo(10L);
        assertThat(responses.get(0).status()).isEqualTo("PENDING");
    }

    @Test
    void getRequestDetail_returnsCouponOperationPayloadForRoot() {
        AdminApprovalService service = createService();
        Admin requester = admin(2L, "coupon@test.com", "coupon", AdminRole.SETTLEMENT_ADMIN);
        CouponEvent event = pendingEvent(CouponEventType.FIRST_COME);
        assignId(event, 100L);
        AdminApprovalRequest request = AdminApprovalRequest.createCouponOperation(
                AdminApprovalOperation.COUPON_EVENT_CREATE,
                requester,
                "create coupon",
                "COUPON_EVENT_CREATE:100"
        );
        assignId(request, 10L);
        AdminCouponApprovalPayload payload = AdminCouponApprovalPayload.forCreate(request, event);
        when(approvalRequestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(approvalPayloadRepository.findByApprovalRequest(request)).thenReturn(Optional.of(payload));

        AdminApprovalRequestResponse response = service.getRequestDetail(principal(1L, AdminRole.ROOT_ADMIN), 10L);

        assertThat(response.operation()).isEqualTo("COUPON_EVENT_CREATE");
        assertThat(response.couponPayload().couponEventId()).isEqualTo(100L);
        assertThat(response.couponPayload().eventType()).isEqualTo("FIRST_COME");
    }

    @Test
    void approveRoleChange_appliesRequestedRoleWhenRootApproves() {
        AdminApprovalService service = createService();
        Admin root = admin(1L, "root@test.com", "root", AdminRole.ROOT_ADMIN);
        Admin requester = admin(2L, "user-admin@test.com", "user-admin", AdminRole.USER_ADMIN);
        Admin target = admin(3L, "product-admin@test.com", "product-admin", AdminRole.PRODUCT_ADMIN);
        AdminApprovalRequest request = AdminApprovalRequest.createRoleChange(
                requester,
                target,
                AdminRole.SETTLEMENT_ADMIN,
                "Temporary settlement backup"
        );
        assignId(request, 10L);
        when(adminRepository.findById(1L)).thenReturn(Optional.of(root));
        when(approvalRequestRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(request));

        AdminApprovalRequestResponse response = service.approve(principal(1L, AdminRole.ROOT_ADMIN), 10L, "Approved");

        assertThat(response.status()).isEqualTo("APPROVED");
        assertThat(response.approverNickname()).isEqualTo("root");
        assertThat(target.getRole()).isEqualTo(AdminRole.SETTLEMENT_ADMIN);
    }

    @Test
    void approveAlreadyRejectedRoleChangeDoesNotApplyRequestedRole() {
        AdminApprovalService service = createService();
        Admin root = admin(1L, "root@test.com", "root", AdminRole.ROOT_ADMIN);
        Admin requester = admin(2L, "user-admin@test.com", "user-admin", AdminRole.USER_ADMIN);
        Admin target = admin(3L, "product-admin@test.com", "product-admin", AdminRole.PRODUCT_ADMIN);
        AdminApprovalRequest request = AdminApprovalRequest.createRoleChange(
                requester,
                target,
                AdminRole.SETTLEMENT_ADMIN,
                "Temporary settlement backup"
        );
        request.reject(root, "Already rejected");
        assignId(request, 10L);
        when(adminRepository.findById(1L)).thenReturn(Optional.of(root));
        when(approvalRequestRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> service.approve(principal(1L, AdminRole.ROOT_ADMIN), 10L, "Approved"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONFLICT);

        assertThat(target.getRole()).isEqualTo(AdminRole.PRODUCT_ADMIN);
    }

    @Test
    void approveCouponCreate_activatesEventAndCreatesSlots() {
        AdminApprovalService service = createService();
        Admin root = admin(1L, "root@test.com", "root", AdminRole.ROOT_ADMIN);
        Admin requester = admin(2L, "coupon@test.com", "coupon", AdminRole.SETTLEMENT_ADMIN);
        CouponEvent event = pendingEvent(CouponEventType.FIRST_COME);
        assignId(event, 100L);
        AdminApprovalRequest request = AdminApprovalRequest.createCouponOperation(
                AdminApprovalOperation.COUPON_EVENT_CREATE,
                requester,
                "create coupon",
                "COUPON_EVENT_CREATE:100"
        );
        assignId(request, 10L);
        AdminCouponApprovalPayload payload = AdminCouponApprovalPayload.forCreate(request, event);
        when(adminRepository.findById(1L)).thenReturn(Optional.of(root));
        when(approvalRequestRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(request));
        when(approvalPayloadRepository.findByApprovalRequest(request)).thenReturn(Optional.of(payload));
        when(couponEventRepository.findById(100L)).thenReturn(Optional.of(event));

        AdminApprovalRequestResponse response = service.approve(principal(1L, AdminRole.ROOT_ADMIN), 10L, "Approved");

        assertThat(response.status()).isEqualTo("APPROVED");
        assertThat(event.getStatus().name()).isEqualTo("ACTIVE");
        assertThat(response.couponPayload().couponEventId()).isEqualTo(100L);
        verify(couponRepository).saveAll(any());
    }

    @Test
    void rejectCouponCreate_marksEventRejectedAndCreatesNoSlots() {
        AdminApprovalService service = createService();
        Admin root = admin(1L, "root@test.com", "root", AdminRole.ROOT_ADMIN);
        Admin requester = admin(2L, "coupon@test.com", "coupon", AdminRole.SETTLEMENT_ADMIN);
        CouponEvent event = pendingEvent(CouponEventType.FIRST_COME);
        assignId(event, 100L);
        AdminApprovalRequest request = AdminApprovalRequest.createCouponOperation(
                AdminApprovalOperation.COUPON_EVENT_CREATE,
                requester,
                "create coupon",
                "COUPON_EVENT_CREATE:100"
        );
        assignId(request, 10L);
        AdminCouponApprovalPayload payload = AdminCouponApprovalPayload.forCreate(request, event);
        when(adminRepository.findById(1L)).thenReturn(Optional.of(root));
        when(approvalRequestRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(request));
        when(approvalPayloadRepository.findByApprovalRequest(request)).thenReturn(Optional.of(payload));
        when(couponEventRepository.findById(100L)).thenReturn(Optional.of(event));

        AdminApprovalRequestResponse response = service.reject(principal(1L, AdminRole.ROOT_ADMIN), 10L, "Rejected");

        assertThat(response.status()).isEqualTo("REJECTED");
        assertThat(event.getStatus().name()).isEqualTo("REJECTED");
        verify(couponRepository, never()).saveAll(any());
    }

    @Test
    void approveCouponStop_marksEventStoppedWithoutChangingCoupons() {
        AdminApprovalService service = createService();
        Admin root = admin(1L, "root@test.com", "root", AdminRole.ROOT_ADMIN);
        Admin requester = admin(2L, "coupon@test.com", "coupon", AdminRole.SETTLEMENT_ADMIN);
        CouponEvent event = activeEvent(CouponEventType.FIRST_COME);
        assignId(event, 100L);
        AdminApprovalRequest request = AdminApprovalRequest.createCouponOperation(
                AdminApprovalOperation.COUPON_EVENT_STOP,
                requester,
                "stop coupon",
                "COUPON_EVENT_STOP:100"
        );
        assignId(request, 10L);
        AdminCouponApprovalPayload payload = AdminCouponApprovalPayload.forStop(request, 100L);
        when(adminRepository.findById(1L)).thenReturn(Optional.of(root));
        when(approvalRequestRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(request));
        when(approvalPayloadRepository.findByApprovalRequest(request)).thenReturn(Optional.of(payload));
        when(couponEventRepository.findById(100L)).thenReturn(Optional.of(event));

        AdminApprovalRequestResponse response = service.approve(principal(1L, AdminRole.ROOT_ADMIN), 10L, "Approved");

        assertThat(response.status()).isEqualTo("APPROVED");
        assertThat(event.getStatus().name()).isEqualTo("STOPPED");
        verify(couponRepository, never()).saveAll(any());
    }

    @Test
    void rejectCouponStop_keepsEventActive() {
        AdminApprovalService service = createService();
        Admin root = admin(1L, "root@test.com", "root", AdminRole.ROOT_ADMIN);
        Admin requester = admin(2L, "coupon@test.com", "coupon", AdminRole.SETTLEMENT_ADMIN);
        CouponEvent event = activeEvent(CouponEventType.FIRST_COME);
        assignId(event, 100L);
        AdminApprovalRequest request = AdminApprovalRequest.createCouponOperation(
                AdminApprovalOperation.COUPON_EVENT_STOP,
                requester,
                "stop coupon",
                "COUPON_EVENT_STOP:100"
        );
        assignId(request, 10L);
        AdminCouponApprovalPayload payload = AdminCouponApprovalPayload.forStop(request, 100L);
        when(adminRepository.findById(1L)).thenReturn(Optional.of(root));
        when(approvalRequestRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(request));
        when(approvalPayloadRepository.findByApprovalRequest(request)).thenReturn(Optional.of(payload));

        AdminApprovalRequestResponse response = service.reject(principal(1L, AdminRole.ROOT_ADMIN), 10L, "Rejected");

        assertThat(response.status()).isEqualTo("REJECTED");
        assertThat(event.getStatus().name()).isEqualTo("ACTIVE");
    }

    @Test
    void approveCouponIssue_usesCouponSlotAssignmentPath() {
        AdminApprovalService service = createService();
        Admin root = admin(1L, "root@test.com", "root", AdminRole.ROOT_ADMIN);
        Admin requester = admin(2L, "coupon@test.com", "coupon", AdminRole.SETTLEMENT_ADMIN);
        CouponEvent event = activeEvent(CouponEventType.ADMIN_INDIVIDUAL);
        assignId(event, 100L);
        AdminApprovalRequest request = AdminApprovalRequest.createCouponOperation(
                AdminApprovalOperation.COUPON_EVENT_ISSUE,
                requester,
                "issue coupon",
                "COUPON_EVENT_ISSUE:100"
        );
        assignId(request, 10L);
        AdminCouponApprovalPayload payload = AdminCouponApprovalPayload.forIssue(
                request,
                100L,
                List.of(7L, 8L),
                2,
                2,
                0,
                0,
                2,
                2,
                false
        );
        when(adminRepository.findById(1L)).thenReturn(Optional.of(root));
        when(approvalRequestRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(request));
        when(approvalPayloadRepository.findByApprovalRequest(request)).thenReturn(Optional.of(payload));
        when(couponEventRepository.findById(100L)).thenReturn(Optional.of(event));

        AdminApprovalRequestResponse response = service.approve(principal(1L, AdminRole.ROOT_ADMIN), 10L, "Approved");

        assertThat(response.status()).isEqualTo("APPROVED");
        verify(couponSlotService).assignSlots(eq(100L), eq(List.of(7L, 8L)));
    }

    @Test
    void approveAlreadyApprovedCouponIssueDoesNotAssignSlotsAgain() {
        AdminApprovalService service = createService();
        Admin root = admin(1L, "root@test.com", "root", AdminRole.ROOT_ADMIN);
        Admin requester = admin(2L, "coupon@test.com", "coupon", AdminRole.SETTLEMENT_ADMIN);
        CouponEvent event = activeEvent(CouponEventType.ADMIN_INDIVIDUAL);
        assignId(event, 100L);
        AdminApprovalRequest request = AdminApprovalRequest.createCouponOperation(
                AdminApprovalOperation.COUPON_EVENT_ISSUE,
                requester,
                "issue coupon",
                "COUPON_EVENT_ISSUE:100"
        );
        request.approve(root, "Already approved");
        assignId(request, 10L);
        AdminCouponApprovalPayload payload = AdminCouponApprovalPayload.forIssue(
                request,
                100L,
                List.of(7L, 8L),
                2,
                2,
                0,
                0,
                2,
                2,
                false
        );
        when(adminRepository.findById(1L)).thenReturn(Optional.of(root));
        when(approvalRequestRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(request));
        when(approvalPayloadRepository.findByApprovalRequest(request)).thenReturn(Optional.of(payload));

        assertThatThrownBy(() -> service.approve(principal(1L, AdminRole.ROOT_ADMIN), 10L, "Approved again"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONFLICT);

        verify(couponSlotService, never()).assignSlots(any(), any());
    }

    @Test
    void approve_rejectsNonRootAdmin() {
        AdminApprovalService service = createService();

        assertThatThrownBy(() -> service.approve(principal(2L, AdminRole.USER_ADMIN), 10L, "nope"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void reject_marksPendingRequestRejectedWithoutApplyingRole() {
        AdminApprovalService service = createService();
        Admin root = admin(1L, "root@test.com", "root", AdminRole.ROOT_ADMIN);
        Admin requester = admin(2L, "user-admin@test.com", "user-admin", AdminRole.USER_ADMIN);
        Admin target = admin(3L, "product-admin@test.com", "product-admin", AdminRole.PRODUCT_ADMIN);
        AdminApprovalRequest request = AdminApprovalRequest.createRoleChange(
                requester,
                target,
                AdminRole.SETTLEMENT_ADMIN,
                "Temporary settlement backup"
        );
        assignId(request, 10L);
        when(adminRepository.findById(1L)).thenReturn(Optional.of(root));
        when(approvalRequestRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(request));

        AdminApprovalRequestResponse response = service.reject(principal(1L, AdminRole.ROOT_ADMIN), 10L, "Need more context");

        assertThat(response.status()).isEqualTo(AdminApprovalStatus.REJECTED.name());
        assertThat(target.getRole()).isEqualTo(AdminRole.PRODUCT_ADMIN);
    }

    private Admin admin(Long id, String email, String nickname, AdminRole role) {
        Admin admin = Admin.create(email, "encoded", nickname, role);
        assignId(admin, id);
        return admin;
    }

    private CouponEvent activeEvent(CouponEventType type) {
        return CouponEvent.create(
                type,
                "coupon",
                3,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                1000,
                0,
                30
        );
    }

    private CouponEvent pendingEvent(CouponEventType type) {
        return CouponEvent.createPending(
                type,
                "coupon",
                3,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                1000,
                0,
                30
        );
    }

    private AdminPrincipal principal(Long adminId, AdminRole role) {
        return new AdminPrincipal(adminId, "admin@test.com", "encoded", role, AdminStatus.ACTIVE, "admin");
    }
}
