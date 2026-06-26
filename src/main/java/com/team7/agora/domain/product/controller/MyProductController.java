// 내가 등록한 상품 목록 조회 API 컨트롤러
package com.team7.agora.domain.product.controller;

import com.team7.agora.domain.product.dto.response.ProductResponse;
import com.team7.agora.domain.product.service.ProductService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 내 상품 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
 */
@RestController
public class MyProductController {

    private final ProductService productService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param productService 상품 비즈니스 로직을 처리하는 서비스
     */
    public MyProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 내 상품 정보를 조회하는 GET /api/users/me/products 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @return 클라이언트에 반환할 API 응답
     */
    @GetMapping("/api/users/me/products")
    public ApiResponse<List<ProductResponse>> getMyProducts(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success("내가 등록한 상품 목록을 조회했습니다.", productService.getMyProducts(userDetails.getUserId()));
    }
}
