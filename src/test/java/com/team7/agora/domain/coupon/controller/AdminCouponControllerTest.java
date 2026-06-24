package com.team7.agora.domain.coupon.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team7.agora.domain.coupon.dto.response.AdminCouponResponse;
import com.team7.agora.domain.coupon.enums.CouponType;
import com.team7.agora.domain.coupon.service.AdminCouponService;
import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.exception.GlobalExceptionHandler;
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
class AdminCouponControllerTest {

    @Mock
    private AdminCouponService adminCouponService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminCouponController(adminCouponService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createUsesAuthenticatedAdminAndReturnsSuccessEnvelope() throws Exception {
        authenticate(UserRole.USER_ADMIN);
        when(adminCouponService.create(
            any(CustomUserDetails.class),
            any(String.class),
            any(Integer.class),
            any(Integer.class),
            any(CouponType.class),
            any(Integer.class)
        )).thenReturn(new AdminCouponResponse(1L, "신규 쿠폰", 5000, 10000, "FIRST_COME", "ACTIVE", 30));

        mockMvc.perform(post("/api/admin/coupons")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name":"신규 쿠폰",
                      "discountAmount":5000,
                      "minOrderAmount":10000,
                      "type":"FIRST_COME",
                      "validDays":30
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.data.couponId").value(1L));

        verify(adminCouponService).create(
            any(CustomUserDetails.class),
            any(String.class),
            any(Integer.class),
            any(Integer.class),
            any(CouponType.class),
            any(Integer.class)
        );
    }

    @Test
    void getDetailUsesAuthenticatedAdminAndReturnsCoupon() throws Exception {
        authenticate(UserRole.ROOT_ADMIN);
        when(adminCouponService.getDetail(any(CustomUserDetails.class), any(Long.class)))
            .thenReturn(new AdminCouponResponse(1L, "신규 쿠폰", 5000, 10000, "FIRST_COME", "ACTIVE", 30));

        mockMvc.perform(get("/api/admin/coupons/{couponId}", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.data.couponId").value(1L))
            .andExpect(jsonPath("$.data.name").value("신규 쿠폰"));

        verify(adminCouponService).getDetail(any(CustomUserDetails.class), any(Long.class));
    }

    @Test
    void createReturnsBadRequestWhenNameIsBlank() throws Exception {
        authenticate(UserRole.USER_ADMIN);

        mockMvc.perform(post("/api/admin/coupons")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name":"",
                      "discountAmount":5000,
                      "minOrderAmount":10000,
                      "type":"FIRST_COME",
                      "validDays":30
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("ERROR"));
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
