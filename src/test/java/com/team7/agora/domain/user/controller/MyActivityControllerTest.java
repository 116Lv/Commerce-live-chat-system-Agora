package com.team7.agora.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team7.agora.domain.trade.dto.request.MyTradeRole;
import com.team7.agora.domain.trade.dto.response.MyTradeResponse;
import com.team7.agora.domain.trade.service.TradeService;
import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.exception.GlobalExceptionHandler;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class MyActivityControllerTest {

    @Mock
    private TradeService tradeService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new MyActivityController(tradeService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getMyTrades_wrapsPagedResponseAndParsesRole() throws Exception {
        authenticate();
        when(tradeService.getMyTrades(eq(1L), eq(MyTradeRole.SELLER), any()))
            .thenReturn(new PageImpl<>(List.of(new MyTradeResponse(
                10L,
                20L,
                "product",
                30000L,
                25000L,
                "PAID",
                "SELLER",
                "buyer",
                "PAID",
                null,
                LocalDateTime.of(2026, 1, 1, 0, 0)
            )), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/users/me/trades")
                .param("role", "seller")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.data.content[0].tradeId").value(10L))
            .andExpect(jsonPath("$.data.content[0].role").value("SELLER"))
            .andExpect(jsonPath("$.data.page").value(0))
            .andExpect(jsonPath("$.data.size").value(20))
            .andExpect(jsonPath("$.data.totalElements").value(1));

        verify(tradeService).getMyTrades(eq(1L), eq(MyTradeRole.SELLER), any());
    }

    @Test
    void getMyTrades_returns400ForInvalidRole() throws Exception {
        authenticate();

        mockMvc.perform(get("/api/users/me/trades").param("role", "viewer"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    void getMyTrades_returns400ForNegativePage() throws Exception {
        authenticate();

        mockMvc.perform(get("/api/users/me/trades").param("page", "-1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    void getMyTrades_returns400ForNonPositiveSize() throws Exception {
        authenticate();

        mockMvc.perform(get("/api/users/me/trades").param("size", "0"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    void getMyTrades_returns400ForNonNumericPage() throws Exception {
        authenticate();

        mockMvc.perform(get("/api/users/me/trades").param("page", "abc"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("ERROR"));
    }

    private void authenticate() {
        CustomUserDetails principal = new CustomUserDetails(
            1L,
            "user@test.com",
            "encoded",
            UserRole.ROLE_USER,
            UserStatus.ACTIVE,
            "user"
        );
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }
}
