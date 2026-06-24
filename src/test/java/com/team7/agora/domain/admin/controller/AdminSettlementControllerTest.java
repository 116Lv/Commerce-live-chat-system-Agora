package com.team7.agora.domain.admin.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team7.agora.domain.admin.dto.response.AdminSettlementResponse;
import com.team7.agora.domain.admin.service.AdminSettlementService;
import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.exception.GlobalExceptionHandler;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AdminSettlementControllerTest {

    @Mock
    private AdminSettlementService adminSettlementService;

    private MockMvc mockMvc;
    private CustomUserDetails admin;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminSettlementController(adminSettlementService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .build();
        admin = new CustomUserDetails(
            99L,
            "settlement@admin.com",
            "encoded",
            UserRole.SETTLEMENT_ADMIN,
            UserStatus.ACTIVE,
            "정산관리자"
        );
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void settleReturnsCommonResponse() throws Exception {
        when(adminSettlementService.settle(admin, 10L)).thenReturn(new AdminSettlementResponse(
            10L,
            1L,
            2L,
            BigDecimal.valueOf(50000),
            "SETTLED",
            LocalDateTime.now()
        ));

        mockMvc.perform(post("/api/admin/settlements/10/settle"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.data.settlementId").value(10L))
            .andExpect(jsonPath("$.data.status").value("SETTLED"));

        verify(adminSettlementService).settle(admin, 10L);
    }
}
