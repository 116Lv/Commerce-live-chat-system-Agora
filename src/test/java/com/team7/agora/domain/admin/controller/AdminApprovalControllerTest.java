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
import com.team7.agora.global.auth.AdminPrincipal;
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
