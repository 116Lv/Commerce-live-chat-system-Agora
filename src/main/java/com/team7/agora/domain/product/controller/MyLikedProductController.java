// 내가 찜한 상품 목록 조회 API 컨트롤러
package com.team7.agora.domain.product.controller;

import com.team7.agora.domain.product.dto.response.ProductResponse;
import com.team7.agora.domain.product.service.ProductLikeService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes my liked product endpoints.
 */
@RestController
public class MyLikedProductController {

    private final ProductLikeService productLikeService;

    /**
     * Creates a my liked product controller instance.
     * @param productLikeService the product like service value
     */
    public MyLikedProductController(ProductLikeService productLikeService) {
        this.productLikeService = productLikeService;
    }

    /**
     * Returns my liked products data.
     * @param userDetails the auth user value
     * @return the get my liked products result
     */
    @GetMapping("/api/users/me/likes")
    public ApiResponse<List<ProductResponse>> getMyLikedProducts(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ProductResponse> response = productLikeService.getMyLikedProducts(userDetails.getUserId());
        return ApiResponse.success("내가 찜한 상품 목록을 조회했습니다.", response);
    }
}
