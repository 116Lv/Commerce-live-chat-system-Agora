package com.team7.agora.global.response;

public record ApiResponse<T>(
        String status,
        String message,
        T data
) {

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("SUCCESS", message, data);
    }

    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>("ERROR", message, null);
    }
}