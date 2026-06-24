// 관리자 API HTTP 계약을 검증하는 컨트롤러 테스트
package com.team7.agora.domain.admin.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team7.agora.domain.admin.dto.response.AdminDashboardResponse;
import com.team7.agora.domain.admin.dto.response.AdminLoginResponse;
import com.team7.agora.domain.admin.dto.response.AdminMeResponse;
import com.team7.agora.domain.admin.dto.response.AdminUserResponse;
import com.team7.agora.domain.admin.service.AdminAccountService;
import com.team7.agora.domain.admin.service.AdminAuthService;
import com.team7.agora.domain.admin.service.AdminService;
import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.exception.GlobalExceptionHandler;
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
class AdminControllerTest {

    @Mock
    private AdminAuthService adminAuthService;

    @Mock
    private AdminService adminService;

    @Mock
    private AdminAccountService adminAccountService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new AdminAuthController(adminAuthService),
                        new AdminController(adminService),
                        new AdminAccountController(adminAccountService)
                )
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private CustomUserDetails authenticate(UserRole role) {
        CustomUserDetails principal = new CustomUserDetails(
                1L, "admin@test.com", "encoded", role, UserStatus.ACTIVE, "관리자");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        return principal;
    }

    @Test
    void login_returnsAdminAccessToken() throws Exception {
        // given
        when(adminAuthService.login(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new AdminLoginResponse("Bearer admin-token"));

        // when & then
        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@test.com","password":"password123!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").value("Bearer admin-token"));
    }

    @Test
    void logout_usesAuthenticatedAdmin() throws Exception {
        // given
        CustomUserDetails principal = authenticate(UserRole.ROOT_ADMIN);

        // when & then
        mockMvc.perform(post("/api/admin/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
        verify(adminAuthService).logout(principal);
    }

    @Test
    void getMe_returnsAdminInfo() throws Exception {
        // given
        authenticate(UserRole.ROOT_ADMIN);
        when(adminService.getMe(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new AdminMeResponse(1L, "admin@test.com", "관리자", "ROOT_ADMIN"));

        // when & then
        mockMvc.perform(get("/api/admin/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("ROOT_ADMIN"));
    }

    @Test
    void getDashboard_returnsRoleMenus() throws Exception {
        // given
        authenticate(UserRole.USER_ADMIN);
        when(adminService.getDashboard(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new AdminDashboardResponse("USER_ADMIN", List.of("USERS", "USER_REPORTS")));

        // when & then
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessibleMenus[0]").value("USERS"));
    }

    @Test
    void changeRole_delegatesWithAuthenticatedRootAdmin() throws Exception {
        // given
        CustomUserDetails principal = authenticate(UserRole.ROOT_ADMIN);
        when(adminAccountService.changeRole(principal, 2L, UserRole.PRODUCT_ADMIN))
                .thenReturn(new AdminUserResponse(2L, "target@test.com", "대상관리자", "PRODUCT_ADMIN", "ACTIVE"));

        // when & then
        mockMvc.perform(patch("/api/admin/accounts/2/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role":"PRODUCT_ADMIN"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("PRODUCT_ADMIN"));
    }
}
