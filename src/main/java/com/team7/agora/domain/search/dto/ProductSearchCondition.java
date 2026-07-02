package com.team7.agora.domain.search.dto;

import com.team7.agora.domain.product.enums.ProductStatus;
import org.springframework.data.domain.Pageable;

/**
 * 상품 검색 조건 데이터를 전달하는 DTO이다.
 * @param keyword 검색어
 * @param regionId 지역 ID
 * @param category 업로드 카테고리
 * @param status 상품 판매 상태
 * @param pageable 페이지 요청 정보
 * @param sort 정렬 기준 (recent, likes)
 * @param direction 정렬 방향 (desc, asc)
 */
public record ProductSearchCondition(
    String keyword,
    Long regionId,
    String category,
    ProductStatus status,
    Pageable pageable,
    String sort,
    String direction
) {

    public ProductSearchCondition(String keyword, Long regionId, String category, Pageable pageable) {
        this(keyword, regionId, category, null, pageable, null, null);
    }

    public ProductSearchCondition(
        String keyword,
        Long regionId,
        String category,
        ProductStatus status,
        Pageable pageable
    ) {
        this(keyword, regionId, category, status, pageable, null, null);
    }

    public ProductSearchCondition(
        String keyword,
        Long regionId,
        String category,
        Pageable pageable,
        String sort,
        String direction
    ) {
        this(keyword, regionId, category, null, pageable, sort, direction);
    }

    /**
     * 'normalizedKeyword' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     * @return 클라이언트에 반환할 API 응답
     */
    public String normalizedKeyword() {
        if (keyword == null) {
            return "";
        }
        return keyword.trim().toLowerCase();
    }

    public String normalizedCategory() {
        if (category == null) {
            return "";
        }
        return category.trim();
    }

    public ProductStatus normalizedStatus() {
        if (status == ProductStatus.SELLING || status == ProductStatus.SOLD) {
            return status;
        }
        return null;
    }

    public String normalizedSort() {
        return (sort == null || sort.isBlank()) ? "recent" : sort.trim();
    }

    public String normalizedDirection() {
        return "asc".equalsIgnoreCase(direction) ? "asc" : "desc";
    }
}
