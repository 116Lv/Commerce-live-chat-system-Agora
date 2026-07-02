package com.team7.agora.domain.admin.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team7.agora.domain.admin.dto.response.AdminApprovalRequestResponse;
import com.team7.agora.domain.admin.dto.response.AdminProductResponse;
import com.team7.agora.domain.admin.enums.AdminRole;
import com.team7.agora.domain.admin.enums.AdminStatus;
import com.team7.agora.domain.admin.service.AdminProductService;
import com.team7.agora.global.auth.AdminPrincipal;
import java.math.BigDecimal;
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
        authenticate(AdminRole.PRODUCT_ADMIN);
        when(adminProductService.getProducts(
                any(AdminPrincipal.class),
                eq(true),
                eq("Bike"),
                eq("seller"),
                eq("SELLING"),
                eq("PENDING"),
                any()
        ))
                .thenReturn(new PageImpl<>(
                        List.of(new AdminProductResponse(
                                1L,
                                "Bike",
                                "Good condition",
                                BigDecimal.valueOf(100000),
                                10L,
                                "seller",
                                "SELLING",
                                "Selling",
                                "PENDING",
                                null,
                                null,
                                List.of(),
                                LocalDateTime.parse("2026-07-02T10:00:00")
                        )),
                        PageRequest.of(0, 20),
                        42
                ));

        mockMvc.perform(get("/api/admin/products")
                        .param("reportedOnly", "true")
                        .param("keyword", "Bike")
                        .param("sellerKeyword", "seller")
                        .param("status", "SELLING")
                        .param("approvalStatus", "PENDING")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content[0].id").value(1L))
                .andExpect(jsonPath("$.data.content[0].title").value("Bike"))
                .andExpect(jsonPath("$.data.content[0].sellerNickname").value("seller"))
                .andExpect(jsonPath("$.data.content[0].approvalStatus").value("PENDING"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.totalElements").value(42))
                .andExpect(jsonPath("$.data.totalPages").value(3));
    }

    @Test
    void getProducts_passesDomainSearchConditionsToService() throws Exception {
        authenticate(AdminRole.PRODUCT_ADMIN);
        when(adminProductService.getProducts(
                any(AdminPrincipal.class),
                eq(false),
                eq("chair"),
                eq("seller01"),
                eq("RESERVED"),
                eq("APPROVED"),
                any()
        )).thenReturn(new PageImpl<>(List.of(), PageRequest.of(1, 10), 0));

        mockMvc.perform(get("/api/admin/products")
                        .param("keyword", "chair")
                        .param("sellerKeyword", "seller01")
                        .param("status", "RESERVED")
                        .param("approvalStatus", "APPROVED")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(10));
    }

    @Test
    void hideProduct_usesAuthenticatedAdminAndReturnsHiddenProduct() throws Exception {
        authenticate(AdminRole.PRODUCT_ADMIN);
        when(adminProductService.hideProduct(any(AdminPrincipal.class), eq(1L)))
                .thenReturn(new AdminProductResponse(
                        1L,
                        "Bike",
                        "Good condition",
                        BigDecimal.valueOf(100000),
                        10L,
                        "seller",
                        "HIDDEN",
                        "Hidden",
                        "REJECTED",
                        null,
                        null,
                        List.of(),
                        LocalDateTime.parse("2026-07-02T10:00:00")
                ));

        mockMvc.perform(patch("/api/admin/products/{productId}/hide", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.status").value("HIDDEN"));
    }

    @Test
    void approveProduct_usesAuthenticatedAdminAndReturnsApprovedProduct() throws Exception {
        authenticate(AdminRole.PRODUCT_ADMIN);
        when(adminProductService.approveProduct(any(AdminPrincipal.class), eq(1L)))
                .thenReturn(new AdminProductResponse(
                        1L,
                        "Bike",
                        "Good condition",
                        BigDecimal.valueOf(100000),
                        10L,
                        "seller",
                        "SELLING",
                        "Selling",
                        "APPROVED",
                        null,
                        null,
                        List.of(),
                        LocalDateTime.parse("2026-07-02T10:00:00")
                ));

        mockMvc.perform(patch("/api/admin/products/{productId}/approve", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.approvalStatus").value("APPROVED"));
    }

    @Test
    void requestHideProduct_usesAuthenticatedAdminAndReturnsApprovalRequest() throws Exception {
        authenticate(AdminRole.PRODUCT_ADMIN);
        when(adminProductService.requestHideProduct(any(AdminPrincipal.class), eq(1L), eq("신고 누적")))
                .thenReturn(new AdminApprovalRequestResponse(
                        15L,
                        "PRODUCT_HIDE",
                        "PENDING",
                        99L,
                        "admin@test.com",
                        "admin",
                        null,
                        null,
                        null,
                        null,
                        1L,
                        "Bike",
                        "신고 누적",
                        null,
                        null,
                        null,
                        null,
                        LocalDateTime.parse("2026-07-02T10:00:00"),
                        null
                ));

        mockMvc.perform(post("/api/admin/products/{productId}/hide-requests", 1L)
                        .contentType("application/json")
                        .content("""
                                {"reason":"신고 누적"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.operation").value("PRODUCT_HIDE"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.targetProductId").value(1L));
    }

    private void authenticate(AdminRole role) {
        AdminPrincipal principal = new AdminPrincipal(
                99L,
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
