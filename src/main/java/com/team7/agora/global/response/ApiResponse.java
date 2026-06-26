package com.team7.agora.global.response;

/**
 * Response payload for returning api data.
 * @param <T> the payload type carried by this component
 * @param status the status value
 * @param message the message value
 * @param data the data value
 */
public record ApiResponse<T>(
        String status,
        String message,
        T data
) {

    /**
     * Handles success behavior.
     * @param message the message value
     * @param data the data value
     * @return the success result
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("SUCCESS", message, data);
    }

    /**
     * Handles error behavior.
     * @param message the message value
     * @return the error result
     */
    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>("ERROR", message, null);
    }
}