package com.team7.agora.domain.coupon.service;

import static com.team7.agora.support.TestEntityIds.assignId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.admin.entity.Admin;
import com.team7.agora.domain.admin.entity.AdminApprovalRequest;
import com.team7.agora.domain.admin.enums.AdminApprovalStatus;
import com.team7.agora.domain.coupon.entity.Coupon;
import com.team7.agora.domain.coupon.entity.CouponEvent;
import com.team7.agora.domain.coupon.enums.CouponEventStatus;
import com.team7.agora.domain.coupon.enums.CouponEventType;
import com.team7.agora.domain.coupon.repository.AdminCouponApprovalPayloadRepository;
import com.team7.agora.domain.coupon.repository.CouponEventRepository;
import com.team7.agora.domain.coupon.repository.CouponRepository;
import com.team7.agora.domain.admin.enums.AdminRole;
import com.team7.agora.domain.admin.enums.AdminStatus;
import com.team7.agora.domain.admin.repository.AdminApprovalRequestRepository;
import com.team7.agora.domain.admin.repository.AdminRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class AdminCouponEventServiceTest {

    @Mock
    private CouponEventRepository couponEventRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponSlotService couponSlotService;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private AdminApprovalRequestRepository approvalRequestRepository;

    @Mock
    private AdminCouponApprovalPayloadRepository approvalPayloadRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void createEventByRootImmediatelyApprovesActivatesEventAndCreatesSlots() {
        Admin root = admin(99L, AdminRole.ROOT_ADMIN);
        when(adminRepository.findById(99L)).thenReturn(Optional.of(root));
        when(couponEventRepository.save(any(CouponEvent.class))).thenAnswer(invocation -> {
            CouponEvent event = invocation.getArgument(0);
            assignId(event, 1L);
            return event;
        });
        when(approvalRequestRepository.save(any(AdminApprovalRequest.class))).thenAnswer(invocation -> {
            AdminApprovalRequest request = invocation.getArgument(0);
            assignId(request, 10L);
            return request;
        });
        when(approvalPayloadRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<java.util.List> slotsCaptor = ArgumentCaptor.forClass(java.util.List.class);

        var response = newService().createEvent(
            principal(AdminRole.ROOT_ADMIN),
            CouponEventType.FIRST_COME,
            "첫 거래 쿠폰",
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(1),
            3,
            5000,
            10000,
            30,
            "Root launch approval"
        );

        org.mockito.Mockito.verify(couponRepository).saveAll(slotsCaptor.capture());
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.operation()).isEqualTo("COUPON_EVENT_CREATE");
        assertThat(response.status()).isEqualTo("APPROVED");
        assertThat(response.couponPayload().couponEventId()).isEqualTo(1L);
        assertThat(response.couponPayload().eventType()).isEqualTo("FIRST_COME");
        assertThat(response.couponPayload().totalQuantity()).isEqualTo(3);
        assertThat(slotsCaptor.getValue()).hasSize(3);
    }

    @Test
    void createEventByNonRootStoresPendingApprovalAndDoesNotSaveSlots() {
        Admin requester = admin(99L, AdminRole.SETTLEMENT_ADMIN);
        when(adminRepository.findById(99L)).thenReturn(Optional.of(requester));
        when(couponEventRepository.save(any(CouponEvent.class))).thenAnswer(invocation -> {
            CouponEvent event = invocation.getArgument(0);
            assignId(event, 1L);
            return event;
        });
        when(approvalRequestRepository.save(any(AdminApprovalRequest.class))).thenAnswer(invocation -> {
            AdminApprovalRequest request = invocation.getArgument(0);
            assignId(request, 10L);
            return request;
        });
        when(approvalPayloadRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = newService().createEvent(
            principal(AdminRole.SETTLEMENT_ADMIN),
            CouponEventType.FIRST_COME,
            "Pending coupon",
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(1),
            3,
            5000,
            10000,
            30,
            "Campaign launch approval"
        );

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.operation()).isEqualTo("COUPON_EVENT_CREATE");
        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.reason()).isEqualTo("Campaign launch approval");
        assertThat(response.couponPayload().couponEventId()).isEqualTo(1L);
        verify(couponRepository, never()).saveAll(any());
        verify(approvalPayloadRepository).save(any());
    }

    @Test
    void getListWithoutStatusShowsOnlyApprovedOperationalEvents() {
        CouponEvent active = event(CouponEventType.FIRST_COME);
        when(couponEventRepository.findAllByStatusIn(List.of(
            CouponEventStatus.ACTIVE,
            CouponEventStatus.STOP_REQUESTED,
            CouponEventStatus.STOPPED,
            CouponEventStatus.ENDED
        ))).thenReturn(List.of(active));

        var responses = newService().getList(principal(AdminRole.SETTLEMENT_ADMIN), null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).status()).isEqualTo("ACTIVE");
        verify(couponEventRepository).findAllByStatusIn(List.of(
            CouponEventStatus.ACTIVE,
            CouponEventStatus.STOP_REQUESTED,
            CouponEventStatus.STOPPED,
            CouponEventStatus.ENDED
        ));
        verify(couponEventRepository, never()).findAll();
    }

    @Test
    void getListWithExplicitStatusCanQueryRejectedRequests() {
        CouponEvent rejected = pendingEvent(CouponEventType.FIRST_COME);
        rejected.reject();
        when(couponEventRepository.findAllByStatus(CouponEventStatus.REJECTED)).thenReturn(List.of(rejected));

        var responses = newService().getList(principal(AdminRole.SETTLEMENT_ADMIN), CouponEventStatus.REJECTED);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).status()).isEqualTo("REJECTED");
        verify(couponEventRepository).findAllByStatus(CouponEventStatus.REJECTED);
        verify(couponEventRepository, never()).findAll();
    }

    @Test
    void requestStopByNonRootCreatesPendingRequestAndLeavesEventActive() {
        Admin requester = admin(99L, AdminRole.SETTLEMENT_ADMIN);
        CouponEvent event = event(CouponEventType.FIRST_COME);
        when(adminRepository.findById(99L)).thenReturn(Optional.of(requester));
        when(couponEventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(approvalRequestRepository.save(any(AdminApprovalRequest.class))).thenAnswer(invocation -> {
            AdminApprovalRequest request = invocation.getArgument(0);
            assignId(request, 10L);
            return request;
        });

        var response = newService().requestStop(principal(AdminRole.SETTLEMENT_ADMIN), 1L, "Stop after campaign");

        assertThat(response.status()).isEqualTo(AdminApprovalStatus.PENDING.name());
        assertThat(event.getStatus().name()).isEqualTo("ACTIVE");
        verify(approvalPayloadRepository).save(any());
    }

    @Test
    void requestStopRejectsNonActiveEventBeforeSavingApprovalRequest() {
        Admin requester = admin(99L, AdminRole.SETTLEMENT_ADMIN);
        CouponEvent event = pendingEvent(CouponEventType.FIRST_COME);
        when(adminRepository.findById(99L)).thenReturn(Optional.of(requester));
        when(couponEventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> newService().requestStop(
            principal(AdminRole.SETTLEMENT_ADMIN),
            1L,
            "Stop pending event"
        ))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.CONFLICT);

        verify(approvalRequestRepository, never()).save(any());
        verify(approvalPayloadRepository, never()).save(any());
    }

    @Test
    void requestStopRejectsDuplicatePendingRequestBeforeSaving() {
        Admin requester = admin(99L, AdminRole.SETTLEMENT_ADMIN);
        CouponEvent event = event(CouponEventType.FIRST_COME);
        when(adminRepository.findById(99L)).thenReturn(Optional.of(requester));
        when(couponEventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(approvalRequestRepository.existsByPendingRequestKey("COUPON_EVENT_STOP:1")).thenReturn(true);

        assertThatThrownBy(() -> newService().requestStop(
            principal(AdminRole.SETTLEMENT_ADMIN),
            1L,
            "Stop after campaign"
        ))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.CONFLICT);

        verify(approvalRequestRepository, never()).save(any());
        verify(approvalPayloadRepository, never()).save(any());
    }

    @Test
    void individualIssueRequestStoresSummaryAndDoesNotIssueUntilApproval() {
        Admin requester = admin(99L, AdminRole.SETTLEMENT_ADMIN);
        CouponEvent event = event(CouponEventType.ADMIN_INDIVIDUAL);
        when(adminRepository.findById(99L)).thenReturn(Optional.of(requester));
        when(couponEventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(100L, UserStatus.ACTIVE)).thenReturn(Optional.of(user(100L)));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(101L, UserStatus.ACTIVE)).thenReturn(Optional.of(user(101L)));
        when(couponRepository.existsByCouponEventIdAndUserId(1L, 100L)).thenReturn(false);
        when(couponRepository.existsByCouponEventIdAndUserId(1L, 101L)).thenReturn(false);
        when(approvalRequestRepository.save(any(AdminApprovalRequest.class))).thenAnswer(invocation -> {
            AdminApprovalRequest request = invocation.getArgument(0);
            assignId(request, 10L);
            return request;
        });
        when(approvalPayloadRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = newService().requestIssueToUsers(
            principal(AdminRole.SETTLEMENT_ADMIN),
            1L,
            Arrays.asList(100L, 100L, 101L, null)
        );

        assertThat(response.inputCount()).isEqualTo(4);
        assertThat(response.validTargetCount()).isEqualTo(2);
        assertThat(response.duplicateCount()).isEqualTo(1);
        assertThat(response.excludedCount()).isEqualTo(1);
        assertThat(response.plannedIssueCount()).isEqualTo(2);
        assertThat(response.expectedIssuedQuantity()).isEqualTo(2);
        assertThat(response.exceedsRemainingQuantity()).isFalse();
        verify(couponSlotService, never()).assignSlots(any(), any());
        verify(approvalPayloadRepository).save(any());
    }

    @Test
    void individualIssueRequestAcceptsNicknameTargets() {
        Admin requester = admin(99L, AdminRole.SETTLEMENT_ADMIN);
        CouponEvent event = event(CouponEventType.ADMIN_INDIVIDUAL);
        User user = user(100L);
        when(adminRepository.findById(99L)).thenReturn(Optional.of(requester));
        when(couponEventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findAllByNicknameAndStatusAndDeletedAtIsNull("dong", UserStatus.ACTIVE))
            .thenReturn(List.of(user));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(100L, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(couponRepository.existsByCouponEventIdAndUserId(1L, 100L)).thenReturn(false);
        when(approvalRequestRepository.save(any(AdminApprovalRequest.class))).thenAnswer(invocation -> {
            AdminApprovalRequest request = invocation.getArgument(0);
            assignId(request, 10L);
            return request;
        });
        ArgumentCaptor<com.team7.agora.domain.coupon.entity.AdminCouponApprovalPayload> payloadCaptor =
            ArgumentCaptor.forClass(com.team7.agora.domain.coupon.entity.AdminCouponApprovalPayload.class);
        when(approvalPayloadRepository.save(payloadCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = newService().requestIssueToUsers(
            principal(AdminRole.SETTLEMENT_ADMIN),
            1L,
            List.of("dong")
        );

        assertThat(response.validTargetCount()).isEqualTo(1);
        assertThat(payloadCaptor.getValue().targetUserIdList()).containsExactly(100L);
        verify(couponSlotService, never()).assignSlots(any(), any());
    }

    @Test
    void individualIssueRequestExcludesAmbiguousNicknameTargets() {
        Admin requester = admin(99L, AdminRole.SETTLEMENT_ADMIN);
        CouponEvent event = event(CouponEventType.ADMIN_INDIVIDUAL);
        when(adminRepository.findById(99L)).thenReturn(Optional.of(requester));
        when(couponEventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findAllByNicknameAndStatusAndDeletedAtIsNull("dong", UserStatus.ACTIVE))
            .thenReturn(List.of(user(100L), user(101L)));
        when(approvalRequestRepository.save(any(AdminApprovalRequest.class))).thenAnswer(invocation -> {
            AdminApprovalRequest request = invocation.getArgument(0);
            assignId(request, 10L);
            return request;
        });
        ArgumentCaptor<com.team7.agora.domain.coupon.entity.AdminCouponApprovalPayload> payloadCaptor =
            ArgumentCaptor.forClass(com.team7.agora.domain.coupon.entity.AdminCouponApprovalPayload.class);
        when(approvalPayloadRepository.save(payloadCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = newService().requestIssueToUsers(
            principal(AdminRole.SETTLEMENT_ADMIN),
            1L,
            List.of("dong")
        );

        assertThat(response.validTargetCount()).isZero();
        assertThat(response.excludedCount()).isEqualTo(1);
        assertThat(payloadCaptor.getValue().targetUserIdList()).isEmpty();
        verify(couponSlotService, never()).assignSlots(any(), any());
    }

    @Test
    void individualIssueSummaryExcludesDuplicatesInvalidUsersAndAlreadyIssuedUsers() {
        Admin requester = admin(99L, AdminRole.SETTLEMENT_ADMIN);
        CouponEvent event = event(CouponEventType.ADMIN_INDIVIDUAL);
        when(adminRepository.findById(99L)).thenReturn(Optional.of(requester));
        when(couponEventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(100L, UserStatus.ACTIVE)).thenReturn(Optional.of(user(100L)));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(101L, UserStatus.ACTIVE)).thenReturn(Optional.of(user(101L)));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(102L, UserStatus.ACTIVE)).thenReturn(Optional.empty());
        when(couponRepository.existsByCouponEventIdAndUserId(1L, 100L)).thenReturn(false);
        when(couponRepository.existsByCouponEventIdAndUserId(1L, 101L)).thenReturn(true);
        when(approvalRequestRepository.save(any(AdminApprovalRequest.class))).thenAnswer(invocation -> {
            AdminApprovalRequest request = invocation.getArgument(0);
            assignId(request, 10L);
            return request;
        });
        when(approvalPayloadRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = newService().requestIssueToUsers(
            principal(AdminRole.SETTLEMENT_ADMIN),
            1L,
            Arrays.asList(100L, 100L, 101L, 102L, null)
        );

        assertThat(response.inputCount()).isEqualTo(5);
        assertThat(response.validTargetCount()).isEqualTo(1);
        assertThat(response.duplicateCount()).isEqualTo(1);
        assertThat(response.excludedCount()).isEqualTo(3);
        assertThat(response.plannedIssueCount()).isEqualTo(1);
        assertThat(response.expectedIssuedQuantity()).isEqualTo(1);
        assertThat(response.exceedsRemainingQuantity()).isFalse();
        verify(couponSlotService, never()).assignSlots(any(), any());
    }

    @Test
    void individualIssueRequestLimitsStoredTargetsAndPlannedCountToRemainingQuantity() {
        Admin requester = admin(99L, AdminRole.SETTLEMENT_ADMIN);
        CouponEvent event = event(CouponEventType.ADMIN_INDIVIDUAL);
        event.issue();
        event.issue();
        event.issue();
        when(adminRepository.findById(99L)).thenReturn(Optional.of(requester));
        when(couponEventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(100L, UserStatus.ACTIVE)).thenReturn(Optional.of(user(100L)));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(101L, UserStatus.ACTIVE)).thenReturn(Optional.of(user(101L)));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(102L, UserStatus.ACTIVE)).thenReturn(Optional.of(user(102L)));
        when(couponRepository.existsByCouponEventIdAndUserId(1L, 100L)).thenReturn(false);
        when(couponRepository.existsByCouponEventIdAndUserId(1L, 101L)).thenReturn(false);
        when(couponRepository.existsByCouponEventIdAndUserId(1L, 102L)).thenReturn(false);
        when(approvalRequestRepository.save(any(AdminApprovalRequest.class))).thenAnswer(invocation -> {
            AdminApprovalRequest request = invocation.getArgument(0);
            assignId(request, 10L);
            return request;
        });
        ArgumentCaptor<com.team7.agora.domain.coupon.entity.AdminCouponApprovalPayload> payloadCaptor =
            ArgumentCaptor.forClass(com.team7.agora.domain.coupon.entity.AdminCouponApprovalPayload.class);
        when(approvalPayloadRepository.save(payloadCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = newService().requestIssueToUsers(
            principal(AdminRole.SETTLEMENT_ADMIN),
            1L,
            List.of(100L, 101L, 102L)
        );

        assertThat(response.validTargetCount()).isEqualTo(3);
        assertThat(response.plannedIssueCount()).isEqualTo(2);
        assertThat(response.expectedIssuedQuantity()).isEqualTo(5);
        assertThat(response.exceedsRemainingQuantity()).isTrue();
        assertThat(payloadCaptor.getValue().targetUserIdList()).containsExactly(100L, 101L);
        verify(couponSlotService, never()).assignSlots(any(), any());
    }

    @Test
    void individualIssueRequestRejectsNonAdminIndividualEventBeforeSavingApprovalRequest() {
        Admin requester = admin(99L, AdminRole.SETTLEMENT_ADMIN);
        CouponEvent event = event(CouponEventType.FIRST_COME);
        when(adminRepository.findById(99L)).thenReturn(Optional.of(requester));
        when(couponEventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> newService().requestIssueToUsers(
            principal(AdminRole.SETTLEMENT_ADMIN),
            1L,
            List.of(100L)
        ))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_REQUEST);

        verify(approvalRequestRepository, never()).save(any());
        verify(approvalPayloadRepository, never()).save(any());
        verify(couponSlotService, never()).assignSlots(any(), any());
    }

    @Test
    void individualIssueRequestRejectsPendingAdminIndividualEventBeforeSavingApprovalRequest() {
        Admin requester = admin(99L, AdminRole.SETTLEMENT_ADMIN);
        CouponEvent event = pendingEvent(CouponEventType.ADMIN_INDIVIDUAL);
        when(adminRepository.findById(99L)).thenReturn(Optional.of(requester));
        when(couponEventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> newService().requestIssueToUsers(
            principal(AdminRole.SETTLEMENT_ADMIN),
            1L,
            List.of(100L)
        ))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.CONFLICT);

        verify(approvalRequestRepository, never()).save(any());
        verify(approvalPayloadRepository, never()).save(any());
        verify(couponSlotService, never()).assignSlots(any(), any());
    }

    @Test
    void individualIssueRequestTranslatesDuplicatePendingRequestRaceToConflict() {
        Admin requester = admin(99L, AdminRole.SETTLEMENT_ADMIN);
        CouponEvent event = event(CouponEventType.ADMIN_INDIVIDUAL);
        when(adminRepository.findById(99L)).thenReturn(Optional.of(requester));
        when(couponEventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(100L, UserStatus.ACTIVE)).thenReturn(Optional.of(user(100L)));
        when(couponRepository.existsByCouponEventIdAndUserId(1L, 100L)).thenReturn(false);
        when(approvalRequestRepository.save(any(AdminApprovalRequest.class)))
            .thenThrow(new DataIntegrityViolationException("duplicate pending request"));

        assertThatThrownBy(() -> newService().requestIssueToUsers(
            principal(AdminRole.SETTLEMENT_ADMIN),
            1L,
            List.of(100L)
        ))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.CONFLICT);

        verify(approvalPayloadRepository, never()).save(any());
        verify(couponSlotService, never()).assignSlots(any(), any());
    }

    @Test
    void createEventRejectsNonAdmin() {
        assertThatThrownBy(() -> newService().createEvent(
            principal(AdminRole.PRODUCT_ADMIN),
            CouponEventType.FIRST_COME,
            "첫 거래 쿠폰",
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(1),
            3,
            5000,
            10000,
            30,
            "Campaign launch approval"
        )).isInstanceOf(BusinessException.class);
    }

    @Test
    void getCouponsIncludesUserEmailAndReadableLabels() {
        CouponEvent event = event(CouponEventType.ADMIN_INDIVIDUAL);
        User user = User.signup("buyer@test.com", "encoded", "buyer", "01012345678");
        assignId(user, 10L);
        Coupon coupon = Coupon.createAvailableSlot(event);
        assignId(coupon, 100L);
        coupon.assign(user, LocalDateTime.now(), event.getValidDays());

        when(couponEventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(couponRepository.findAllByCouponEventIdAndUserIsNotNull(1L)).thenReturn(List.of(coupon));

        var responses = newService().getCoupons(principal(AdminRole.ROOT_ADMIN), 1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).userNickname()).isEqualTo("buyer");
        assertThat(responses.get(0).userEmail()).isEqualTo("buyer@test.com");
        assertThat(responses.get(0).statusLabel()).isEqualTo("Issued");
    }

    private AdminCouponEventService newService() {
        return new AdminCouponEventService(
            couponEventRepository,
            couponRepository,
            couponSlotService,
            adminRepository,
            approvalRequestRepository,
            approvalPayloadRepository,
            userRepository
        );
    }

    private CouponEvent event(CouponEventType type) {
        CouponEvent event = CouponEvent.create(
            type,
            "Admin coupon",
            5,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(1),
            5000,
            10000,
            30
        );
        assignId(event, 1L);
        return event;
    }

    private AdminPrincipal principal(AdminRole role) {
        return new AdminPrincipal(99L, "admin@test.com", "encoded", role, AdminStatus.ACTIVE, "admin");
    }

    private Admin admin(Long id, AdminRole role) {
        Admin admin = Admin.create("admin" + id + "@test.com", "encoded", "admin" + id, role);
        assignId(admin, id);
        return admin;
    }

    private CouponEvent pendingEvent(CouponEventType type) {
        CouponEvent event = CouponEvent.createPending(
            type,
            "Admin coupon",
            5,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(1),
            5000,
            10000,
            30
        );
        assignId(event, 1L);
        return event;
    }

    private User user(Long id) {
        User user = User.signup("user" + id + "@test.com", "encoded", "user" + id, "01012345678");
        assignId(user, id);
        return user;
    }
}
