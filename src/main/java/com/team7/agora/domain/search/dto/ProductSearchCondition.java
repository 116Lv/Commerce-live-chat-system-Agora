package com.team7.agora.domain.search.dto;

import org.springframework.data.domain.Pageable;

/**
 * 상품 검색 조건 데이터를 전달하는 DTO이다.
 * @param keyword 검색어
 * @param regionId 지역 ID
 * @param category 업로드 카테고리
 * @param pageable 페이지 요청 정보
 * @param sort 정렬 기준 (recent, likes)
 * @param direction 정렬 방향 (desc, asc)
 */
public record ProductSearchCondition(
    String keyword,
    Long regionId,
    String category,
    Pageable pageable,
    String sort,
    String direction
) {

    public ProductSearchCondition(String keyword, Long regionId, String category, Pageable pageable) {
        this(keyword, regionId, category, pageable, null, null);
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

    public String normalizedSort() {
        return (sort == null || sort.isBlank()) ? "recent" : sort.trim();
    }

    public String normalizedDirection() {
        return "asc".equalsIgnoreCase(direction) ? "asc" : "desc";
    }
}
