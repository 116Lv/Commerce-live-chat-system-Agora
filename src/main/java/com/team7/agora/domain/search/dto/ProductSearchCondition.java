package com.team7.agora.domain.search.dto;

import org.springframework.data.domain.Pageable;

/**
 * 데이터 전송에 사용하는 DTO이다.
 * @param keyword 입력 값
 * @param regionId 입력 값
 * @param category 입력 값
 * @param pageable 입력 값
 */
public record ProductSearchCondition(
    String keyword,
    Long regionId,
    String category,
    Pageable pageable
) {

    /**
     * 요청한 동작을 처리한다.
     * @return 처리 결과
     */
    public String normalizedKeyword() {
        if (keyword == null) {
            return "";
        }
        return keyword.trim().toLowerCase();
    }
}
