// 상품 찜 등록/취소 API 컨트롤러
package com.team7.agora.domain.product.controller;

import com.team7.agora.domain.product.dto.response.ProductLikeResponse;
import com.team7.agora.domain.product.service.ProductLikeService;
import com.team7.agora.global.auth.AuthUser;
import com.team7.agora.global.response.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes product like endpoints.
 */
@RestController
@RequestMapping("/api/products/{productId}/likes")
public class ProductLikeController {

    private final ProductLikeService productLikeService;

    /**
     * Creates a product like controller instance.
     * @param productLikeService the product like service value
     */
    public ProductLikeController(ProductLikeService productLikeService) {
        this.productLikeService = productLikeService;
    }

    /**
     * Handles like behavior.
     * @param authUser the auth user value
     * @param productId the product id value
     * @return the like result
     */
    @PostMapping
    public ApiResponse<ProductLikeResponse> like(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long productId
    ) {
        ProductLikeResponse response = productLikeService.like(authUser.userId(), productId);
        return ApiResponse.success("상품을 찜했습니다.", response);
    }

    /**
     * Handles unlike behavior.
     * @param authUser the auth user value
     * @param productId the product id value
     * @return the unlike result
     */
    @DeleteMapping
    public ApiResponse<ProductLikeResponse> unlike(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long productId
    ) {
        ProductLikeResponse response = productLikeService.unlike(authUser.userId(), productId);
        return ApiResponse.success("상품 찜을 취소했습니다.", response);
    }
}
