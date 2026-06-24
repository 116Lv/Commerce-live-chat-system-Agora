// 내가 등록한 상품 목록 조회 API 컨트롤러
package com.team7.agora.domain.product.controller;

import com.team7.agora.domain.product.dto.response.ProductResponse;
import com.team7.agora.domain.product.service.ProductService;
import com.team7.agora.global.auth.AuthUser;
import com.team7.agora.global.response.ApiResponse;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyProductController {

    private final ProductService productService;

    public MyProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/api/users/me/products")
    public ApiResponse<List<ProductResponse>> getMyProducts(@AuthenticationPrincipal AuthUser authUser) {
        return ApiResponse.success("내가 등록한 상품 목록을 조회했습니다.", productService.getMyProducts(authUser.userId()));
    }
}
