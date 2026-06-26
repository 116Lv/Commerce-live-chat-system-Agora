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
 * REST 엔드포인트를 제공하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/products")
public class ProductImageController {

    private final ProductImageService productImageService;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param productImageService 입력 값
     */
    public ProductImageController(ProductImageService productImageService) {
        this.productImageService = productImageService;
    }

    /**
     * 요청한 동작을 처리한다.
     * @param userDetails 입력 값
     * @param productId 입력 값
     * @param image 입력 값
     * @return 처리 결과
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
