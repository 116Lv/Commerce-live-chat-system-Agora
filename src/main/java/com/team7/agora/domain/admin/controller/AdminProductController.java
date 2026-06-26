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
 * REST 엔드포인트를 제공하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final AdminProductService adminProductService;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param adminProductService 입력 값
     */
    public AdminProductController(AdminProductService adminProductService) {
        this.adminProductService = adminProductService;
    }

    /**
     * 데이터를 반환한다.
     * @param admin 입력 값
     * @param reportedOnly 입력 값
     * @param page 입력 값
     * @param size 입력 값
     * @return 처리 결과
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
     * 요청한 동작을 처리한다.
     * @param admin 입력 값
     * @param productId 입력 값
     * @return 처리 결과
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
