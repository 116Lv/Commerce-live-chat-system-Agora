package com.team7.agora.domain.admin.controller;

import com.team7.agora.domain.admin.dto.response.AdminProductResponse;
import com.team7.agora.domain.admin.service.AdminProductService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final AdminProductService adminProductService;

    public AdminProductController(AdminProductService adminProductService) {
        this.adminProductService = adminProductService;
    }

    @GetMapping
    public ApiResponse<List<AdminProductResponse>> getProducts(
            @AuthenticationPrincipal CustomUserDetails admin,
            @RequestParam(defaultValue = "false") boolean reportedOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<AdminProductResponse> responses = adminProductService.getProducts(admin, reportedOnly, PageRequest.of(page, size));
        return ApiResponse.success("상품 목록을 조회했습니다.", responses);
    }

    @PatchMapping("/{productId}/hide")
    public ApiResponse<AdminProductResponse> hideProduct(
            @AuthenticationPrincipal CustomUserDetails admin,
            @PathVariable Long productId
    ) {
        AdminProductResponse response = adminProductService.hideProduct(admin, productId);
        return ApiResponse.success("상품이 숨김 처리되었습니다.", response);
    }
}
