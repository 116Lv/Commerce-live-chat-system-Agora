package com.team7.agora.global.response;

/**
 * 응답 본문을 전달하는 DTO이다.
 * @param <T> 이 컴포넌트가 전달하는 페이로드 타입
 * @param status 입력 값
 * @param message 입력 값
 * @param data 입력 값
 */
public record ApiResponse<T>(
        String status,
        String message,
        T data
) {

    /**
     * 요청한 동작을 처리한다.
     * @param message 입력 값
     * @param data 입력 값
     * @return 처리 결과
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("SUCCESS", message, data);
    }

    /**
     * 요청한 동작을 처리한다.
     * @param message 입력 값
     * @return 처리 결과
     */
    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>("ERROR", message, null);
    }
}