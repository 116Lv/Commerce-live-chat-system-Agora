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
 * REST 엔드포인트를 제공하는 컨트롤러이다.
 */
@RestController
public class MyProductController {

    private final ProductService productService;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param productService 입력 값
     */
    public MyProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 데이터를 반환한다.
     * @param userDetails 입력 값
     * @return 처리 결과
     */
    @GetMapping("/api/users/me/products")
    public ApiResponse<List<ProductResponse>> getMyProducts(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success("내가 등록한 상품 목록을 조회했습니다.", productService.getMyProducts(userDetails.getUserId()));
    }
}
