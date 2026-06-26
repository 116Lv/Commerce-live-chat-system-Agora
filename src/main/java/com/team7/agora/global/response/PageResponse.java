package com.team7.agora.global.response;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * 응답 본문을 전달하는 DTO이다.
 * @param <T> 이 컴포넌트가 전달하는 페이로드 타입
 * @param content 입력 값
 * @param page 입력 값
 * @param size 입력 값
 * @param totalElements 입력 값
 * @param totalPages 입력 값
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    /**
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param page 입력 값
     * @return 처리 결과
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
