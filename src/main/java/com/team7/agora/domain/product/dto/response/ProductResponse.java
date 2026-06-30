package com.team7.agora.domain.product.dto.response;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.enums.ProductApprovalStatus;
import com.team7.agora.domain.product.enums.ProductStatus;
import java.math.BigDecimal;

/**
 * 상품 응답 본문을 표현하는 DTO이다.
 * @param productId 상품 ID
 * @param title 상품 제목 또는 화면에 표시할 제목
 * @param description 상품 설명 또는 상세 내용
 * @param price 가격
 * @param category 업로드 카테고리
 * @param status 조회 또는 변경할 상태
 */
public record ProductResponse(
    Long productId,
    String title,
    String description,
    BigDecimal price,
    String category,
    ProductStatus status,
    int likeCount,
    boolean liked,
    Long regionId,
    String regionName,
    String sido,
    String sigungu,
    String eupmyeondong,
    Long sellerId,
    String sellerNickname,
    String primaryImageUrl,
    String thumbnailUrl,
    String statusLabel,
    String categoryLabel,
    ProductApprovalStatus approvalStatus
) {

    public ProductResponse(
        Long productId,
        String title,
        String description,
        BigDecimal price,
        String category,
        ProductStatus status
    ) {
        this(
            productId,
            title,
            description,
            price,
            category,
            status,
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
            statusLabel(status),
            category,
            null
        );
    }

    /**
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param product 상품 엔티티
     * @return 클라이언트에 반환할 API 응답
     */
    public static ProductResponse from(Product product) {
        return from(product, false, null);
    }

    public static ProductResponse from(Product product, String primaryImageUrl) {
        return from(product, false, primaryImageUrl);
    }

    public static ProductResponse from(Product product, boolean liked, String primaryImageUrl) {
        return new ProductResponse(
            product.getId(),
            product.getTitle(),
            product.getDescription(),
            product.getPrice(),
            product.getCategory(),
            product.getStatus(),
            product.getLikeCount(),
            liked,
            product.getRegion().getId(),
            product.getRegion().getName(),
            product.getRegion().getSido(),
            product.getRegion().getSigungu(),
            product.getRegion().getEupmyeondong(),
            product.getSeller().getId(),
            product.getSeller().getNickname(),
            primaryImageUrl,
            primaryImageUrl,
            statusLabel(product.getStatus()),
            product.getCategory(),
            product.getApprovalStatus()
        );
    }

    private static String statusLabel(ProductStatus status) {
        return status == null ? null : status.getDisplayLabel();
    }
}
