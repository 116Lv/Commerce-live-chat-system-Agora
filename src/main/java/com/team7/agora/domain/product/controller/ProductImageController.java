// 상품 이미지 업로드 API 컨트롤러
package com.team7.agora.domain.product.controller;

import com.team7.agora.domain.product.dto.response.ProductImageResponse;
import com.team7.agora.domain.product.service.ProductImageService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 상품 이미지 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/products")
public class ProductImageController {

    private final ProductImageService productImageService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param productImageService 해당 기능의 비즈니스 로직을 처리하는 서비스
     */
    public ProductImageController(ProductImageService productImageService) {
        this.productImageService = productImageService;
    }

    /**
     * 상품 이미지 정보를 생성하거나 준비하는 POST /api/products/{productId}/images 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param productId 대상 상품 ID
     * @param image 업로드할 이미지 파일
     * @return 클라이언트에 반환할 API 응답
     */
    @PostMapping("/{productId}/images")
    public ResponseEntity<ApiResponse<ProductImageResponse>> upload(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long productId,
        @RequestParam("image") MultipartFile image
    ) {
        ProductImageResponse response = productImageService.upload(userDetails.getUserId(), productId, image);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("상품 이미지가 업로드되었습니다.", response));
    }
}
