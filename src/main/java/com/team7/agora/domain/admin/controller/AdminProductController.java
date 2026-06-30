package com.team7.agora.domain.admin.controller;

import com.team7.agora.domain.admin.dto.response.AdminProductResponse;
import com.team7.agora.domain.admin.service.AdminProductService;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.response.ApiResponse;
import com.team7.agora.global.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasAnyAuthority('ROOT_ADMIN', 'PRODUCT_ADMIN')")
public class AdminProductController {

    private final AdminProductService adminProductService;

    public AdminProductController(AdminProductService adminProductService) {
        this.adminProductService = adminProductService;
    }

    @GetMapping
    public ApiResponse<PageResponse<AdminProductResponse>> getProducts(
            @AuthenticationPrincipal AdminPrincipal admin,
            @RequestParam(defaultValue = "false") boolean reportedOnly,
            @RequestParam(required = false) String approvalStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<AdminProductResponse> responses = adminProductService.getProducts(
                admin,
                reportedOnly,
                approvalStatus,
                PageRequest.of(page, size)
        );
        return ApiResponse.success("Products have been loaded.", PageResponse.from(responses));
    }

    @PatchMapping("/{productId}/hide")
    public ApiResponse<AdminProductResponse> hideProduct(
            @AuthenticationPrincipal AdminPrincipal admin,
            @PathVariable Long productId
    ) {
        AdminProductResponse response = adminProductService.hideProduct(admin, productId);
        return ApiResponse.success("Product has been hidden.", response);
    }

    @PatchMapping("/{productId}/approve")
    public ApiResponse<AdminProductResponse> approveProduct(
            @AuthenticationPrincipal AdminPrincipal admin,
            @PathVariable Long productId
    ) {
        AdminProductResponse response = adminProductService.approveProduct(admin, productId);
        return ApiResponse.success("Product has been approved.", response);
    }
}
