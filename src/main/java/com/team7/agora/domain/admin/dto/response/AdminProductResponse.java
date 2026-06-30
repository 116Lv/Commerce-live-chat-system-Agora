package com.team7.agora.domain.admin.dto.response;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.enums.ProductApprovalStatus;
import com.team7.agora.domain.product.enums.ProductStatus;
import java.math.BigDecimal;

public record AdminProductResponse(
        Long id,
        String title,
        BigDecimal price,
        Long sellerId,
        String sellerNickname,
        String status,
        String statusLabel,
        String approvalStatus
) {

    public static AdminProductResponse from(Product product) {
        ProductStatus status = product.getStatus();
        return new AdminProductResponse(
                product.getId(),
                product.getTitle(),
                product.getPrice(),
                product.getSeller().getId(),
                product.getSeller().getNickname(),
                status.name(),
                status.getDisplayLabel(),
                approvalStatus(product.getApprovalStatus())
        );
    }

    private static String approvalStatus(ProductApprovalStatus approvalStatus) {
        return approvalStatus == null ? null : approvalStatus.name();
    }
}
