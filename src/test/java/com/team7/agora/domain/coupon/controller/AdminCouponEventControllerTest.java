package com.team7.agora.domain.coupon.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team7.agora.domain.admin.dto.response.AdminApprovalRequestResponse;
import com.team7.agora.domain.admin.enums.AdminRole;
import com.team7.agora.domain.admin.enums.AdminStatus;
import com.team7.agora.domain.coupon.dto.response.AdminCouponApprovalPayloadResponse;
import com.team7.agora.domain.coupon.dto.response.AdminCouponEventResponse;
import com.team7.agora.domain.coupon.dto.response.AdminCouponIssueApprovalResponse;
import com.team7.agora.domain.coupon.enums.CouponEventStatus;
import com.team7.agora.domain.coupon.enums.CouponEventType;
import com.team7.agora.domain.coupon.service.AdminCouponEventService;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import com.team7.agora.global.exception.GlobalExceptionHandler;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AdminCouponEventControllerTest {

    @Mock
    private AdminCouponEventService adminCouponEventService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminCouponEventController(adminCouponEventService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createReturnsApprovalRequestResponse() throws Exception {
        authenticate(AdminRole.ROOT_ADMIN);
        when(adminCouponEventService.createEvent(
                any(AdminPrincipal.class),
                eq(CouponEventType.FIRST_COME),
                eq("Welcome coupon"),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(10),
                eq(5000),
                eq(10000),
                eq(30),
                eq("Campaign launch approval")
            ))
            .thenReturn(couponApprovalResponse("COUPON_EVENT_CREATE", "APPROVED"));

        mockMvc.perform(post("/api/admin/coupon-events/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "type": "FIRST_COME",
                      "name": "Welcome coupon",
                      "startAt": "2026-01-01T00:00:00",
                      "endAt": "2026-12-31T23:59:59",
                      "totalQuantity": 10,
                      "discountAmount": 5000,
                      "minOrderAmount": 10000,
                      "validDays": 30,
                      "reason": "Campaign launch approval"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.message").value("쿠폰 이벤트 생성 승인요청이 등록되었습니다."))
            .andExpect(jsonPath("$.data.id").value(10L))
            .andExpect(jsonPath("$.data.operation").value("COUPON_EVENT_CREATE"))
            .andExpect(jsonPath("$.data.status").value("APPROVED"))
            .andExpect(jsonPath("$.data.couponPayload.couponEventId").value(1L))
            .andExpect(jsonPath("$.data.couponPayload.eventType").value("FIRST_COME"))
            .andExpect(jsonPath("$.data.couponPayload.totalQuantity").value(10));
    }

    @Test
    void createRejectsInvalidRequestBody() throws Exception {
        authenticate(AdminRole.ROOT_ADMIN);

        mockMvc.perform(post("/api/admin/coupon-events/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "type": "FIRST_COME",
                      "name": "",
                      "startAt": "2026-01-01T00:00:00",
                      "endAt": "2026-12-31T23:59:59",
                      "totalQuantity": 10,
                      "discountAmount": 5000,
                      "minOrderAmount": 10000,
                      "validDays": 30,
                      "reason": "Campaign launch approval"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    void createRejectsNonAdminThroughServiceGuard() throws Exception {
        authenticate(AdminRole.PRODUCT_ADMIN);
        when(adminCouponEventService.createEvent(
                any(AdminPrincipal.class),
                any(CouponEventType.class),
                any(String.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(10),
                eq(5000),
                eq(10000),
                eq(30),
                eq("Campaign launch approval")
            ))
            .thenThrow(new BusinessException(ErrorCode.FORBIDDEN, "Only coupon managers can manage coupon events."));

        mockMvc.perform(post("/api/admin/coupon-events/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "type": "FIRST_COME",
                      "name": "Welcome coupon",
                      "startAt": "2026-01-01T00:00:00",
                      "endAt": "2026-12-31T23:59:59",
                      "totalQuantity": 10,
                      "discountAmount": 5000,
                      "minOrderAmount": 10000,
                      "validDays": 30,
                      "reason": "Campaign launch approval"
                    }
                    """))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    void getListUsesOperationalDefaultWhenStatusIsMissing() throws Exception {
        authenticate(AdminRole.SETTLEMENT_ADMIN);
        when(adminCouponEventService.getList(any(AdminPrincipal.class), isNull()))
            .thenReturn(List.of(couponEventResponse("ACTIVE")));

        mockMvc.perform(get("/api/admin/coupon-events"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));

        verify(adminCouponEventService).getList(any(AdminPrincipal.class), isNull());
    }

    @Test
    void getListPassesExplicitStatusFilter() throws Exception {
        authenticate(AdminRole.SETTLEMENT_ADMIN);
        when(adminCouponEventService.getList(any(AdminPrincipal.class), eq(CouponEventStatus.PENDING_APPROVAL)))
            .thenReturn(List.of(couponEventResponse("PENDING_APPROVAL")));

        mockMvc.perform(get("/api/admin/coupon-events").param("status", "PENDING_APPROVAL"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.data[0].status").value("PENDING_APPROVAL"));

        verify(adminCouponEventService).getList(any(AdminPrincipal.class), eq(CouponEventStatus.PENDING_APPROVAL));
    }

    @Test
    void issueReturnsApprovalSummaryResponse() throws Exception {
        authenticate(AdminRole.SETTLEMENT_ADMIN);
        when(adminCouponEventService.requestIssueToUsers(any(AdminPrincipal.class), eq(1L), eq(List.of("10", "11"))))
            .thenReturn(new AdminCouponIssueApprovalResponse(10L, "PENDING", 1L, 2, 2, 0, 0, 2, 2, false));

        mockMvc.perform(post("/api/admin/coupon-events/{eventId}/issue-requests", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "userIds": [10, 11]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.message").value("쿠폰 개별발급 승인요청이 등록되었습니다."))
            .andExpect(jsonPath("$.data.approvalRequestId").value(10L))
            .andExpect(jsonPath("$.data.status").value("PENDING"))
            .andExpect(jsonPath("$.data.inputCount").value(2))
            .andExpect(jsonPath("$.data.validTargetCount").value(2))
            .andExpect(jsonPath("$.data.duplicateCount").value(0))
            .andExpect(jsonPath("$.data.excludedCount").value(0))
            .andExpect(jsonPath("$.data.plannedIssueCount").value(2))
            .andExpect(jsonPath("$.data.expectedIssuedQuantity").value(2))
            .andExpect(jsonPath("$.data.exceedsRemainingQuantity").value(false));

        verify(adminCouponEventService).requestIssueToUsers(any(AdminPrincipal.class), eq(1L), eq(List.of("10", "11")));
    }

    @Test
    void stopReturnsApprovalRequestResponse() throws Exception {
        authenticate(AdminRole.SETTLEMENT_ADMIN);
        when(adminCouponEventService.requestStop(any(AdminPrincipal.class), eq(1L), eq("Campaign ended")))
            .thenReturn(couponApprovalResponse("COUPON_EVENT_STOP", "PENDING"));

        mockMvc.perform(post("/api/admin/coupon-events/{eventId}/stop-requests", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reason": "Campaign ended"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.message").value("쿠폰 이벤트 중단 승인요청이 등록되었습니다."))
            .andExpect(jsonPath("$.data.operation").value("COUPON_EVENT_STOP"))
            .andExpect(jsonPath("$.data.status").value("PENDING"))
            .andExpect(jsonPath("$.data.couponPayload.couponEventId").value(1L));
    }

    @Test
    void stopRejectsMissingReasonBeforeServiceCall() throws Exception {
        authenticate(AdminRole.SETTLEMENT_ADMIN);

        mockMvc.perform(post("/api/admin/coupon-events/{eventId}/stop-requests", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reason": ""
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("ERROR"));

        verify(adminCouponEventService, never()).requestStop(any(), any(), any());
    }

    private AdminApprovalRequestResponse couponApprovalResponse(String operation, String status) {
        return new AdminApprovalRequestResponse(
            10L,
            operation,
            status,
            99L,
            "admin@test.com",
            "admin",
            null,
            null,
            null,
            null,
            "coupon operation",
            99L,
            "admin",
            "Approved",
            null,
            null,
            new AdminCouponApprovalPayloadResponse(
                1L,
                "FIRST_COME",
                "Welcome coupon",
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 12, 31, 23, 59),
                10,
                5000,
                10000,
                30,
                List.of(),
                0,
                0,
                0,
                0,
                0,
                0,
                false
            )
        );
    }

    private AdminCouponEventResponse couponEventResponse(String status) {
        return new AdminCouponEventResponse(
            1L,
            "FIRST_COME",
            "Welcome coupon",
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 12, 31, 23, 59),
            10,
            0,
            5000,
            10000,
            30,
            status,
            10,
            0.0,
            true,
            false,
            false,
            status,
            "FIRST_COME"
        );
    }

    private void authenticate(AdminRole role) {
        AdminPrincipal principal = new AdminPrincipal(
            99L,
            "admin@test.com",
            "encoded",
            role,
            AdminStatus.ACTIVE,
            "admin"
        );
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }
}
