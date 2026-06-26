package com.team7.agora.domain.coupon.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team7.agora.domain.coupon.dto.response.AdminCouponEventResponse;
import com.team7.agora.domain.coupon.dto.response.CouponEventIssueResponse;
import com.team7.agora.domain.coupon.enums.CouponEventType;
import com.team7.agora.domain.coupon.service.AdminCouponEventService;
import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.global.auth.CustomUserDetails;
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
    void createReturnsCommonResponse() throws Exception {
        authenticate(UserRole.ROOT_ADMIN);
        when(adminCouponEventService.createEvent(
                any(CustomUserDetails.class),
                eq(CouponEventType.FIRST_COME),
                eq("동네 첫 거래 선착순 쿠폰"),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(10),
                eq(5000),
                eq(10000),
                eq(30)
            ))
            .thenReturn(new AdminCouponEventResponse(
                1L,
                "FIRST_COME",
                "동네 첫 거래 선착순 쿠폰",
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 12, 31, 23, 59),
                10,
                0,
                5000,
                10000,
                30,
                "ACTIVE"
            ));

        mockMvc.perform(post("/api/admin/coupon-events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "type": "FIRST_COME",
                      "name": "동네 첫 거래 선착순 쿠폰",
                      "startAt": "2026-01-01T00:00:00",
                      "endAt": "2026-12-31T23:59:59",
                      "totalQuantity": 10,
                      "discountAmount": 5000,
                      "minOrderAmount": 10000,
                      "validDays": 30
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.data.eventId").value(1L))
            .andExpect(jsonPath("$.data.type").value("FIRST_COME"));
    }

    @Test
    void createRejectsInvalidRequestBody() throws Exception {
        authenticate(UserRole.ROOT_ADMIN);

        mockMvc.perform(post("/api/admin/coupon-events")
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
                      "validDays": 30
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    void createRejectsNonAdminThroughServiceGuard() throws Exception {
        authenticate(UserRole.ROLE_USER);
        when(adminCouponEventService.createEvent(
                any(CustomUserDetails.class),
                any(CouponEventType.class),
                any(String.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(10),
                eq(5000),
                eq(10000),
                eq(30)
            ))
            .thenThrow(new BusinessException(ErrorCode.FORBIDDEN, "쿠폰 이벤트 관리는 관리자만 수행할 수 있습니다."));

        mockMvc.perform(post("/api/admin/coupon-events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "type": "FIRST_COME",
                      "name": "동네 첫 거래 선착순 쿠폰",
                      "startAt": "2026-01-01T00:00:00",
                      "endAt": "2026-12-31T23:59:59",
                      "totalQuantity": 10,
                      "discountAmount": 5000,
                      "minOrderAmount": 10000,
                      "validDays": 30
                    }
                    """))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    void issueReturnsCommonResponse() throws Exception {
        authenticate(UserRole.USER_ADMIN);
        when(adminCouponEventService.issueToUsers(any(CustomUserDetails.class), eq(1L), eq(List.of(10L, 11L))))
            .thenReturn(new CouponEventIssueResponse(1L, 2, 0));

        mockMvc.perform(post("/api/admin/coupon-events/{eventId}/issue", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "userIds": [10, 11]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.data.issuedCount").value(2));

        verify(adminCouponEventService).issueToUsers(any(CustomUserDetails.class), eq(1L), eq(List.of(10L, 11L)));
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
