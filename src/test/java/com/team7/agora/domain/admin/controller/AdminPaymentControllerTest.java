package com.team7.agora.domain.admin.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team7.agora.domain.admin.dto.response.AdminPaymentResponse;
import com.team7.agora.domain.admin.service.AdminPaymentService;
import com.team7.agora.domain.payment.enums.PaymentStatus;
import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.exception.GlobalExceptionHandler;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AdminPaymentControllerTest {

    @Mock
    private AdminPaymentService adminPaymentService;

    private MockMvc mockMvc;
    private CustomUserDetails admin;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminPaymentController(adminPaymentService))
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
    void getPaymentsReturnsCommonResponse() throws Exception {
        when(adminPaymentService.getPayments(admin, PaymentStatus.PAID, PageRequest.of(0, 20)))
            .thenReturn(List.of(response(1L, "PAID")));

        mockMvc.perform(get("/api/admin/payments")
                .param("status", "PAID")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.data[0].paymentId").value(1L))
            .andExpect(jsonPath("$.data[0].settlementId").value(101L))
            .andExpect(jsonPath("$.data[0].settlementStatus").value("READY"))
            .andExpect(jsonPath("$.data[0].status").value("PAID"));

        verify(adminPaymentService).getPayments(admin, PaymentStatus.PAID, PageRequest.of(0, 20));
    }

    @Test
    void verifyPaymentReturnsCommonResponse() throws Exception {
        when(adminPaymentService.verifyPayment(admin, 1L)).thenReturn(response(1L, "PAID"));

        mockMvc.perform(post("/api/admin/payments/1/verify"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.data.paymentId").value(1L))
            .andExpect(jsonPath("$.data.settlementId").value(101L))
            .andExpect(jsonPath("$.data.settlementStatus").value("READY"));
    }

    @Test
    void getRefundsReturnsCommonResponse() throws Exception {
        when(adminPaymentService.getRefunds(admin)).thenReturn(List.of(response(2L, "REFUNDED")));

        mockMvc.perform(get("/api/admin/refunds"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.data[0].settlementId").value(102L))
            .andExpect(jsonPath("$.data[0].settlementStatus").value("READY"))
            .andExpect(jsonPath("$.data[0].status").value("REFUNDED"));

        verify(adminPaymentService).getRefunds(eq(admin));
    }

    private AdminPaymentResponse response(Long paymentId, String status) {
        return new AdminPaymentResponse(
            paymentId,
            100L + paymentId,
            "READY",
            BigDecimal.valueOf(50000),
            "order-" + paymentId,
            "payment-key-" + paymentId,
            status,
            LocalDateTime.now()
        );
    }
}
