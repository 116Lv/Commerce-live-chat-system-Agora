package com.team7.agora.domain.admin.dto.response;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.enums.ProductApprovalStatus;
import com.team7.agora.domain.product.enums.ProductStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AdminProductResponse(
        Long id,
        String title,
        String description,
        BigDecimal price,
        Long sellerId,
        String sellerNickname,
        String status,
        String statusLabel,
        String approvalStatus,
        String primaryImageUrl,
        String thumbnailUrl,
        List<String> imageUrls,
        LocalDateTime createdAt
) {

    public AdminProductResponse {
        imageUrls = imageUrls == null ? List.of() : List.copyOf(imageUrls);
    }

    public static AdminProductResponse from(Product product) {
        return from(product, List.of());
    }

    public static AdminProductResponse from(Product product, List<String> imageUrls) {
        ProductStatus status = product.getStatus();
        String primaryImageUrl = imageUrls == null || imageUrls.isEmpty() ? null : imageUrls.get(0);
        return new AdminProductResponse(
                product.getId(),
                product.getTitle(),
                product.getDescription(),
                product.getPrice(),
                product.getSeller().getId(),
                product.getSeller().getNickname(),
                status.name(),
                status.getDisplayLabel(),
                approvalStatus(product.getApprovalStatus()),
                primaryImageUrl,
                primaryImageUrl,
                imageUrls,
                product.getCreatedAt()
        );
    }

    private static String approvalStatus(ProductApprovalStatus approvalStatus) {
        return approvalStatus == null ? null : approvalStatus.name();
    }
}
