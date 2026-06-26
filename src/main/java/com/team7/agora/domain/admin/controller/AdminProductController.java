package com.team7.agora.domain.admin.controller;

import com.team7.agora.domain.admin.dto.response.AdminProductResponse;
import com.team7.agora.domain.admin.service.AdminProductService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import com.team7.agora.global.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes admin product endpoints.
 */
@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final AdminProductService adminProductService;

    /**
     * Creates a admin product controller instance.
     * @param adminProductService the admin product service value
     */
    public AdminProductController(AdminProductService adminProductService) {
        this.adminProductService = adminProductService;
    }

    /**
     * Returns products data.
     * @param admin the admin value
     * @param reportedOnly the reported only value
     * @param page the page value
     * @param size the size value
     * @return the get products result
     */
    @GetMapping
    public ApiResponse<PageResponse<AdminProductResponse>> getProducts(
            @AuthenticationPrincipal CustomUserDetails admin,
            @RequestParam(defaultValue = "false") boolean reportedOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<AdminProductResponse> responses = adminProductService.getProducts(admin, reportedOnly, PageRequest.of(page, size));
        return ApiResponse.success("상품 목록을 조회했습니다.", PageResponse.from(responses));
    }

    /**
     * Handles hide product behavior.
     * @param admin the admin value
     * @param productId the product id value
     * @return the hide product result
     */
    @PatchMapping("/{productId}/hide")
    public ApiResponse<AdminProductResponse> hideProduct(
            @AuthenticationPrincipal CustomUserDetails admin,
            @PathVariable Long productId
    ) {
        AdminProductResponse response = adminProductService.hideProduct(admin, productId);
        return ApiResponse.success("상품이 숨김 처리되었습니다.", response);
    }
}
