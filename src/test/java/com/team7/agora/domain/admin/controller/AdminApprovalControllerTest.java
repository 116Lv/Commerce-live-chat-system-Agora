package com.team7.agora.domain.admin.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team7.agora.domain.admin.dto.response.AdminApprovalRequestResponse;
import com.team7.agora.domain.admin.enums.AdminRole;
import com.team7.agora.domain.admin.enums.AdminStatus;
import com.team7.agora.domain.admin.service.AdminApprovalService;
import com.team7.agora.domain.coupon.dto.response.AdminCouponApprovalPayloadResponse;
import com.team7.agora.global.auth.AdminPrincipal;
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
class AdminApprovalControllerTest {

    @Mock
    private AdminApprovalService adminApprovalService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminApprovalController(adminApprovalService))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getApprovalRequests_returnsRootApprovalQueue() throws Exception {
        authenticate(1L, AdminRole.ROOT_ADMIN);
        when(adminApprovalService.getRequests(any(AdminPrincipal.class), eq("PENDING")))
                .thenReturn(List.of(response("PENDING")));

        mockMvc.perform(get("/api/admin/approval-requests").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].operation").value("ADMIN_ROLE_CHANGE"))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }

    @Test
    void getApprovalRequests_includesCouponOperationPayload() throws Exception {
        authenticate(1L, AdminRole.ROOT_ADMIN);
        when(adminApprovalService.getRequests(any(AdminPrincipal.class), eq("PENDING")))
                .thenReturn(List.of(couponResponse("COUPON_EVENT_ISSUE", "PENDING")));

        mockMvc.perform(get("/api/admin/approval-requests").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].operation").value("COUPON_EVENT_ISSUE"))
                .andExpect(jsonPath("$.data[0].couponPayload.couponEventId").value(100L))
                .andExpect(jsonPath("$.data[0].couponPayload.targetUserIds[0]").value(7L))
                .andExpect(jsonPath("$.data[0].couponPayload.inputCount").value(3))
                .andExpect(jsonPath("$.data[0].couponPayload.validTargetCount").value(2))
                .andExpect(jsonPath("$.data[0].couponPayload.duplicateCount").value(1))
                .andExpect(jsonPath("$.data[0].couponPayload.excludedCount").value(0))
                .andExpect(jsonPath("$.data[0].couponPayload.plannedIssueCount").value(2))
                .andExpect(jsonPath("$.data[0].couponPayload.expectedIssuedQuantity").value(2))
                .andExpect(jsonPath("$.data[0].couponPayload.exceedsRemainingQuantity").value(false));
    }

    @Test
    void getApprovalRequestDetail_includesCouponOperationPayload() throws Exception {
        authenticate(1L, AdminRole.ROOT_ADMIN);
        when(adminApprovalService.getRequestDetail(any(AdminPrincipal.class), eq(10L)))
                .thenReturn(couponResponse("COUPON_EVENT_ISSUE", "PENDING"));

        mockMvc.perform(get("/api/admin/approval-requests/{requestId}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.operation").value("COUPON_EVENT_ISSUE"))
                .andExpect(jsonPath("$.data.couponPayload.couponEventId").value(100L))
                .andExpect(jsonPath("$.data.couponPayload.plannedIssueCount").value(2));
    }

    @Test
    void createMyRoleChangeRequest_returnsCreatedRequest() throws Exception {
        authenticate(2L, AdminRole.USER_ADMIN);
        when(adminApprovalService.requestRoleChange(any(AdminPrincipal.class), eq(3L), eq(AdminRole.SETTLEMENT_ADMIN), eq("Need backup")))
                .thenReturn(response("PENDING"));

        mockMvc.perform(post("/api/admin/my-approval-requests/role-change")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"targetAdminId":3,"requestedRole":"SETTLEMENT_ADMIN","reason":"Need backup"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetAdminId").value(3L))
                .andExpect(jsonPath("$.data.requestedRole").value("SETTLEMENT_ADMIN"));
    }

    @Test
    void approveRequest_delegatesToRootDecisionEndpoint() throws Exception {
        authenticate(1L, AdminRole.ROOT_ADMIN);
        when(adminApprovalService.approve(any(AdminPrincipal.class), eq(10L), eq("Approved")))
                .thenReturn(response("APPROVED"));

        mockMvc.perform(post("/api/admin/approval-requests/{requestId}/approve", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"memo":"Approved"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    void approveCouponOperation_returnsPayloadSummary() throws Exception {
        authenticate(1L, AdminRole.ROOT_ADMIN);
        when(adminApprovalService.approve(any(AdminPrincipal.class), eq(10L), eq("Approved")))
                .thenReturn(couponResponse("COUPON_EVENT_ISSUE", "APPROVED"));

        mockMvc.perform(post("/api/admin/approval-requests/{requestId}/approve", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"memo":"Approved"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.couponPayload.plannedIssueCount").value(2));
    }

    @Test
    void rejectRequest_delegatesToRootDecisionEndpoint() throws Exception {
        authenticate(1L, AdminRole.ROOT_ADMIN);
        when(adminApprovalService.reject(any(AdminPrincipal.class), eq(10L), eq("Missing reason")))
                .thenReturn(response("REJECTED"));

        mockMvc.perform(post("/api/admin/approval-requests/{requestId}/reject", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"memo":"Missing reason"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    void rejectCouponOperation_returnsPayloadSummary() throws Exception {
        authenticate(1L, AdminRole.ROOT_ADMIN);
        when(adminApprovalService.reject(any(AdminPrincipal.class), eq(10L), eq("No target users")))
                .thenReturn(couponResponse("COUPON_EVENT_ISSUE", "REJECTED"));

        mockMvc.perform(post("/api/admin/approval-requests/{requestId}/reject", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"memo":"No target users"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"))
                .andExpect(jsonPath("$.data.couponPayload.expectedIssuedQuantity").value(2));
    }

    private AdminApprovalRequestResponse response(String status) {
        return new AdminApprovalRequestResponse(
                10L,
                "ADMIN_ROLE_CHANGE",
                status,
                2L,
                "user-admin@test.com",
                "user-admin",
                3L,
                "product-admin@test.com",
                "product-admin",
                "SETTLEMENT_ADMIN",
                "Need backup",
                1L,
                "root",
                "Reviewed",
                null,
                null
        );
    }

    private AdminApprovalRequestResponse couponResponse(String operation, String status) {
        return new AdminApprovalRequestResponse(
                10L,
                operation,
                status,
                2L,
                "coupon-admin@test.com",
                "coupon-admin",
                null,
                null,
                null,
                null,
                "coupon operation",
                1L,
                "root",
                "Reviewed",
                null,
                null,
                new AdminCouponApprovalPayloadResponse(
                        100L,
                        "ADMIN_INDIVIDUAL",
                        "VIP coupon",
                        LocalDateTime.of(2026, 7, 1, 0, 0),
                        LocalDateTime.of(2026, 7, 31, 23, 59),
                        10,
                        5000,
                        10000,
                        30,
                        List.of(7L, 8L),
                        3,
                        2,
                        1,
                        0,
                        2,
                        2,
                        false
                )
        );
    }

    private void authenticate(Long adminId, AdminRole role) {
        AdminPrincipal principal = new AdminPrincipal(
                adminId,
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
