// 상품 찜 등록/취소 API 컨트롤러
package com.team7.agora.domain.product.controller;

import com.team7.agora.domain.product.dto.response.ProductLikeResponse;
import com.team7.agora.domain.product.service.ProductLikeService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 상품 좋아요 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/products/{productId}/likes")
public class ProductLikeController {

    private final ProductLikeService productLikeService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param productLikeService 해당 기능의 비즈니스 로직을 처리하는 서비스
     */
    public ProductLikeController(ProductLikeService productLikeService) {
        this.productLikeService = productLikeService;
    }

    /**
     * 상품 좋아요 상태를 변경하는 POST /api/products/{productId}/likes 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param productId 대상 상품 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @PostMapping
    public ApiResponse<ProductLikeResponse> like(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long productId
    ) {
        ProductLikeResponse response = productLikeService.like(userDetails.getUserId(), productId);
        return ApiResponse.success("상품을 찜했습니다.", response);
    }

    /**
     * 상품 좋아요 상태를 변경하는 DELETE /api/products/{productId}/likes 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param productId 대상 상품 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @DeleteMapping
    public ApiResponse<ProductLikeResponse> unlike(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long productId
    ) {
        ProductLikeResponse response = productLikeService.unlike(userDetails.getUserId(), productId);
        return ApiResponse.success("상품 찜을 취소했습니다.", response);
    }
}
