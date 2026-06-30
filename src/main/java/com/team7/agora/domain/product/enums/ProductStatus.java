package com.team7.agora.domain.product.enums;

/**
 * 지원하는 상태 또는 유형 값을 정의한다.
 */
public enum ProductStatus {
    SELLING("판매중"),
    RESERVED("예약중"),
    SOLD("판매완료"),
    HIDDEN("숨김"),
    DELETED("삭제됨");

    private final String displayLabel;

    ProductStatus(String displayLabel) {
        this.displayLabel = displayLabel;
    }

    public String getDisplayLabel() {
        return displayLabel;
    }
}
