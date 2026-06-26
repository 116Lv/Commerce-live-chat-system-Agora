package com.team7.agora.global.response;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * 페이지 응답 응답 본문을 표현하는 DTO이다.
 * @param <T> 이 컴포넌트가 전달하는 페이로드 타입
 * @param content 내용
 * @param page 페이지 번호
 * @param size 조회할 메시지 개수
 * @param totalElements 전체 데이터 개수
 * @param totalPages 전체 페이지 수
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
     * @param page 페이지 번호
     * @return 클라이언트에 반환할 API 응답
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
