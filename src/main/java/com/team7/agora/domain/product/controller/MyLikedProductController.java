// 내가 찜한 상품 목록 조회 API 컨트롤러
package com.team7.agora.domain.product.controller;

import com.team7.agora.domain.product.dto.response.ProductResponse;
import com.team7.agora.domain.product.service.ProductLikeService;
import com.team7.agora.global.auth.AuthUser;
import com.team7.agora.global.response.ApiResponse;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyLikedProductController {

    private final ProductLikeService productLikeService;

    public MyLikedProductController(ProductLikeService productLikeService) {
        this.productLikeService = productLikeService;
    }

    @GetMapping("/api/users/me/likes")
    public ApiResponse<List<ProductResponse>> getMyLikedProducts(@AuthenticationPrincipal AuthUser authUser) {
        List<ProductResponse> response = productLikeService.getMyLikedProducts(authUser.userId());
        return ApiResponse.success("내가 찜한 상품 목록을 조회했습니다.", response);
    }
}
