package com.team7.agora.global.response;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Response payload for returning page data.
 * @param <T> the payload type carried by this component
 * @param content the content value
 * @param page the page value
 * @param size the size value
 * @param totalElements the total elements value
 * @param totalPages the total pages value
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    /**
     * Creates a response from the given domain object.
     * @param page the page value
     * @return the from result
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
