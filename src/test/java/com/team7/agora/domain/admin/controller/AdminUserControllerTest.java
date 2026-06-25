package com.team7.agora.domain.admin.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team7.agora.domain.admin.dto.response.AdminUserResponse;
import com.team7.agora.domain.admin.service.AdminUserService;
import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.global.auth.CustomUserDetails;
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
class AdminUserControllerTest {

    @Mock
    private AdminUserService adminUserService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminUserController(adminUserService))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getUsers_usesAuthenticatedAdminAndReturnsUsers() throws Exception {
        authenticate(UserRole.USER_ADMIN);
        when(adminUserService.getUsers(any(CustomUserDetails.class), any()))
                .thenReturn(List.of(new AdminUserResponse(1L, "user@test.com", "동네유저", "ROLE_USER", "ACTIVE")));

        mockMvc.perform(get("/api/admin/users")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].email").value("user@test.com"));
    }

    @Test
    void changeStatus_usesAuthenticatedAdminAndReturnsUpdatedUser() throws Exception {
        authenticate(UserRole.USER_ADMIN);
        when(adminUserService.changeStatus(any(CustomUserDetails.class), eq(1L), eq(UserStatus.BLOCKED)))
                .thenReturn(new AdminUserResponse(1L, "user@test.com", "동네유저", "ROLE_USER", "BLOCKED"));

        mockMvc.perform(patch("/api/admin/users/{userId}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"BLOCKED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.status").value("BLOCKED"));
    }

    private void authenticate(UserRole role) {
        CustomUserDetails principal = new CustomUserDetails(
                99L,
                "admin@test.com",
                "encoded",
                role,
                UserStatus.ACTIVE,
                "관리자"
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }
}
