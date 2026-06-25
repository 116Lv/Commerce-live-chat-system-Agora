package com.team7.agora.domain.coupon.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team7.agora.domain.coupon.service.CouponIssueService;
import com.team7.agora.domain.coupon.service.CouponQueryService;
import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.exception.GlobalExceptionHandler;
import com.team7.agora.global.response.ApiResponse;
import java.util.List;
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
class CouponEventControllerTest {

    @Mock
    private CouponIssueService couponIssueService;

    @Mock
    private CouponQueryService couponQueryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CouponEventController(couponIssueService, couponQueryService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listReturnsSuccessEnvelope() {
        when(couponQueryService.listActiveEvents()).thenReturn(List.of());
        CouponEventController controller = new CouponEventController(couponIssueService, couponQueryService);

        ApiResponse<?> response = controller.list();

        assertThat(response.status()).isEqualTo("SUCCESS");
        assertThat(response.data()).isEqualTo(List.of());
    }

    @Test
    void issueUsesAuthenticatedUserId() throws Exception {
        authenticate();

        mockMvc.perform(post("/api/coupon-events/10/issue"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(couponIssueService).issue(1L, 10L);
    }

    private void authenticate() {
        CustomUserDetails principal = new CustomUserDetails(
            1L,
            "user@test.com",
            "encoded",
            UserRole.ROLE_USER,
            UserStatus.ACTIVE,
            "nickname"
        );
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }
}
