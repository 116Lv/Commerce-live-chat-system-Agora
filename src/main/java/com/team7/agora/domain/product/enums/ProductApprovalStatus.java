package com.team7.agora.domain.product.enums;

public enum ProductApprovalStatus {
    PENDING("승인대기"),
    APPROVED("승인완료"),
    REJECTED("반려");

    private final String displayLabel;

    ProductApprovalStatus(String displayLabel) {
        this.displayLabel = displayLabel;
    }

    public String getDisplayLabel() {
        return displayLabel;
    }
}
