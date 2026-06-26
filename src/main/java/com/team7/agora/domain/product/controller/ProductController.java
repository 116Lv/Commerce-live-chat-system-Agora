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
 * 상품 기능에서 클라이언트의 HTTP 요청을 받아 서비스 계층으로 전달하는 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param productService 상품 비즈니스 로직을 처리하는 서비스
     */
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 상품 정보를 생성하거나 준비하는 POST /api/products 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param request 클라이언트가 전달한 요청 본문
     * @return 클라이언트에 반환할 API 응답
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
     * 상품 정보를 조회하는 GET /api/products/{productId} 요청을 처리한다.
     * @param productId 대상 상품 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable Long productId) {
        ProductResponse response = productService.getProduct(productId);
        return ResponseEntity.ok(ApiResponse.success("상품을 조회했습니다.", response));
    }

    /**
     * 상품 정보를 조회하는 GET /api/products 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param regionId 지역 ID
     * @param page 조회할 페이지 번호
     * @param size 한 번에 조회할 항목 개수
     * @return 클라이언트에 반환할 API 응답
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
     * 상품 상태를 변경하는 PATCH /api/products/{productId} 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param productId 대상 상품 ID
     * @param request 클라이언트가 전달한 요청 본문
     * @return 클라이언트에 반환할 API 응답
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
     * 상품 상태를 변경하는 DELETE /api/products/{productId} 요청을 처리한다.
     * @param userDetails 현재 로그인한 사용자 정보
     * @param productId 대상 상품 ID
     * @return 클라이언트에 반환할 API 응답
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
