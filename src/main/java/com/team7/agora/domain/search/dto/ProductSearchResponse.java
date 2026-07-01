package com.team7.agora.domain.search.dto;

import com.team7.agora.domain.product.enums.ProductStatus;
import java.math.BigDecimal;

/**
 * 상품 검색 데이터를 전달하는 DTO이다.
 * @param id 식별자
 * @param title 상품 제목 또는 화면에 표시할 제목
 * @param price 가격
 * @param regionFullName 거래 지역 전체 이름
 */
public record ProductSearchResponse(
    Long id,
    String title,
    BigDecimal price,
    String regionFullName,
    int likeCount,
    boolean liked,
    Long regionId,
    String sido,
    String sigungu,
    String eupmyeondong,
    String category,
    ProductStatus status,
    Long sellerId,
    String sellerNickname,
    String primaryImageUrl,
    String thumbnailUrl,
    String statusLabel,
    String categoryLabel
) {

    public ProductSearchResponse(Long id, String title, BigDecimal price, String regionFullName) {
        this(
            id,
            title,
            price,
            regionFullName,
            0,
            false,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            statusLabel(null),
            null
        );
    }

    public ProductSearchResponse(
        Long id,
        String title,
        BigDecimal price,
        Long regionId,
        String regionFullName,
        String sido,
        String sigungu,
        String eupmyeondong,
        String category,
        ProductStatus status,
        int likeCount,
        Long sellerId,
        String sellerNickname,
        String primaryImageUrl
    ) {
        // Search responses are cached by query/page, so viewer-specific liked state stays false.
        this(
            id,
            title,
            price,
            regionFullName,
            likeCount,
            false,
            regionId,
            sido,
            sigungu,
            eupmyeondong,
            category,
            status,
            sellerId,
            sellerNickname,
            primaryImageUrl,
            primaryImageUrl,
            statusLabel(status),
            category
        );
    }

    private static String statusLabel(ProductStatus status) {
        return status == null ? null : status.getDisplayLabel();
    }
}
