package com.team7.agora.domain.admin.controller;

import com.team7.agora.domain.admin.dto.response.AdminProductResponse;
import com.team7.agora.domain.admin.service.AdminProductService;
import com.team7.agora.global.auth.AdminPrincipal;
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
 * 관리자 상품 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final AdminProductService adminProductService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param adminProductService 해당 기능의 비즈니스 로직을 처리하는 서비스
     */
    public AdminProductController(AdminProductService adminProductService) {
        this.adminProductService = adminProductService;
    }

    /**
     * 관리자 상품 정보를 조회하는 GET /api/admin/products 요청을 처리한다.
     * @param admin 현재 로그인한 관리자 정보
     * @param reportedOnly 신고된 상품만 조회할지 여부
     * @param page 조회할 페이지 번호
     * @param size 한 번에 조회할 항목 개수
     * @return 클라이언트에 반환할 API 응답
     */
    @GetMapping
    public ApiResponse<PageResponse<AdminProductResponse>> getProducts(
            @AuthenticationPrincipal AdminPrincipal admin,
            @RequestParam(defaultValue = "false") boolean reportedOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<AdminProductResponse> responses = adminProductService.getProducts(admin, reportedOnly, PageRequest.of(page, size));
        return ApiResponse.success("상품 목록을 조회했습니다.", PageResponse.from(responses));
    }

    /**
     * 관리자 상품 상태를 변경하는 PATCH /api/admin/products/{productId}/hide 요청을 처리한다.
     * @param admin 현재 로그인한 관리자 정보
     * @param productId 대상 상품 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @PatchMapping("/{productId}/hide")
    public ApiResponse<AdminProductResponse> hideProduct(
            @AuthenticationPrincipal AdminPrincipal admin,
            @PathVariable Long productId
    ) {
        AdminProductResponse response = adminProductService.hideProduct(admin, productId);
        return ApiResponse.success("상품이 숨김 처리되었습니다.", response);
    }
}
