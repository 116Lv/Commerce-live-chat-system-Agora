// 상품 이미지 업로드 API 컨트롤러
package com.team7.agora.domain.product.controller;

import com.team7.agora.domain.product.dto.response.ProductImageResponse;
import com.team7.agora.domain.product.service.ProductImageService;
import com.team7.agora.global.auth.AuthUser;
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
 * REST controller that exposes product image endpoints.
 */
@RestController
@RequestMapping("/api/products")
public class ProductImageController {

    private final ProductImageService productImageService;

    /**
     * Creates a product image controller instance.
     * @param productImageService the product image service value
     */
    public ProductImageController(ProductImageService productImageService) {
        this.productImageService = productImageService;
    }

    /**
     * Handles upload behavior.
     * @param authUser the auth user value
     * @param productId the product id value
     * @param image the image value
     * @return the upload result
     */
    @PostMapping("/{productId}/images")
    public ResponseEntity<ApiResponse<ProductImageResponse>> upload(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long productId,
        @RequestParam("image") MultipartFile image
    ) {
        ProductImageResponse response = productImageService.upload(authUser.userId(), productId, image);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("상품 이미지가 업로드되었습니다.", response));
    }
}
