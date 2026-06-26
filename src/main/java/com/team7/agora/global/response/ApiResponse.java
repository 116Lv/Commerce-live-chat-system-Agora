package com.team7.agora.global.response;

/**
 * 공통 API 응답 응답 본문을 표현하는 DTO이다.
 * @param <T> 이 컴포넌트가 전달하는 페이로드 타입
 * @param status 조회 또는 변경할 상태
 * @param message 메시지
 * @param data 응답 데이터
 */
public record ApiResponse<T>(
        String status,
        String message,
        T data
) {

    /**
     * 성공 응답 본문을 공통 API 응답 형식으로 감싼다.
     * @param message 메시지
     * @param data 응답 데이터
     * @return 클라이언트에 반환할 API 응답
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("SUCCESS", message, data);
    }

    /**
     * 'error' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     * @param message 메시지
     * @return 클라이언트에 반환할 API 응답
     */
    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>("ERROR", message, null);
    }
}