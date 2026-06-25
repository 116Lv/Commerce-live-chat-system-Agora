package com.team7.agora.domain.admin.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team7.agora.domain.admin.dto.response.AdminProductResponse;
import com.team7.agora.domain.admin.service.AdminProductService;
import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.global.auth.CustomUserDetails;
import java.math.BigDecimal;
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
class AdminProductControllerTest {

    @Mock
    private AdminProductService adminProductService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminProductController(adminProductService))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getProducts_usesAuthenticatedAdminAndReturnsProducts() throws Exception {
        authenticate(UserRole.PRODUCT_ADMIN);
        when(adminProductService.getProducts(any(CustomUserDetails.class), eq(true), any()))
                .thenReturn(new PageImpl<>(
                        List.of(new AdminProductResponse(1L, "중고 자전거", BigDecimal.valueOf(100000), 10L, "SELLING")),
                        PageRequest.of(0, 20),
                        42
                ));

        mockMvc.perform(get("/api/admin/products")
                        .param("reportedOnly", "true")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content[0].id").value(1L))
                .andExpect(jsonPath("$.data.content[0].title").value("중고 자전거"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.totalElements").value(42))
                .andExpect(jsonPath("$.data.totalPages").value(3));
    }

    @Test
    void hideProduct_usesAuthenticatedAdminAndReturnsHiddenProduct() throws Exception {
        authenticate(UserRole.PRODUCT_ADMIN);
        when(adminProductService.hideProduct(any(CustomUserDetails.class), eq(1L)))
                .thenReturn(new AdminProductResponse(1L, "중고 자전거", BigDecimal.valueOf(100000), 10L, "HIDDEN"));

        mockMvc.perform(patch("/api/admin/products/{productId}/hide", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.status").value("HIDDEN"));
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
