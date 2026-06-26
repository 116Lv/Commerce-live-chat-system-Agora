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
 * REST 엔드포인트를 제공하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/products/{productId}/likes")
public class ProductLikeController {

    private final ProductLikeService productLikeService;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param productLikeService 입력 값
     */
    public ProductLikeController(ProductLikeService productLikeService) {
        this.productLikeService = productLikeService;
    }

    /**
     * 요청한 동작을 처리한다.
     * @param userDetails 입력 값
     * @param productId 입력 값
     * @return 처리 결과
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
     * 요청한 동작을 처리한다.
     * @param userDetails 입력 값
     * @param productId 입력 값
     * @return 처리 결과
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
