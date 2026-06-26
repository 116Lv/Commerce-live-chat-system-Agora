package com.team7.agora.domain.product.controller;

import com.team7.agora.domain.product.dto.request.ProductCreateRequest;
import com.team7.agora.domain.product.dto.request.ProductUpdateRequest;
import com.team7.agora.domain.product.dto.response.ProductResponse;
import com.team7.agora.domain.product.service.ProductService;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST 엔드포인트를 제공하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param productService 입력 값
     */
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 도메인 객체를 생성한다.
     * @param userDetails 입력 값
     * @param request 입력 값
     * @return 처리 결과
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> create(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody ProductCreateRequest request
    ) {
        ProductResponse response = productService.create(userDetails.getUserId(), request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("상품이 등록되었습니다.", response));
    }

    /**
     * 데이터를 반환한다.
     * @param productId 입력 값
     * @return 처리 결과
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable Long productId) {
        ProductResponse response = productService.getProduct(productId);
        return ResponseEntity.ok(ApiResponse.success("상품을 조회했습니다.", response));
    }

    /**
     * 데이터를 반환한다.
     * @param userDetails 입력 값
     * @param regionId 입력 값
     * @param page 입력 값
     * @param size 입력 값
     * @return 처리 결과
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProducts(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam(required = false) Long regionId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Long viewerId = userDetails == null ? null : userDetails.getUserId();
        List<ProductResponse> responses = productService.getProducts(viewerId, regionId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success("상품 목록을 조회했습니다.", responses));
    }

    /**
     * 데이터를 수정한다.
     * @param userDetails 입력 값
     * @param productId 입력 값
     * @param request 입력 값
     * @return 처리 결과
     */
    @PatchMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long productId,
        @Valid @RequestBody ProductUpdateRequest request
    ) {
        ProductResponse response = productService.update(userDetails.getUserId(), productId, request);
        return ResponseEntity.ok(ApiResponse.success("상품이 수정되었습니다.", response));
    }

    /**
     * 데이터를 삭제한다.
     * @param userDetails 입력 값
     * @param productId 입력 값
     * @return 처리 결과
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> delete(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long productId
    ) {
        productService.delete(userDetails.getUserId(), productId);
        return ResponseEntity.ok(ApiResponse.success("상품이 삭제되었습니다.", null));
    }
}
