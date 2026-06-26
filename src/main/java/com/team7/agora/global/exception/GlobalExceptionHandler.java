// 컨트롤러 전역에서 예외를 공통 응답 포맷으로 변환하는 핸들러
package com.team7.agora.global.exception;

import com.team7.agora.global.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Domain exception used for global exception failures.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Handles handle business exception behavior.
     * @param exception the exception value
     * @return the handle business exception result
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        return ResponseEntity
                .status(exception.getStatus())
                .body(ApiResponse.error(exception.getMessage()));
    }

    /**
     * Handles handle validation exception behavior.
     * @param exception the exception value
     * @return the handle validation exception result
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage() == null
                        ? ErrorCode.INVALID_REQUEST.getMessage()
                        : error.getDefaultMessage())
                .orElse(ErrorCode.INVALID_REQUEST.getMessage());

        return ResponseEntity
                .status(ErrorCode.INVALID_REQUEST.getStatus())
                .body(ApiResponse.error(message));
    }

    /**
     * Handles handle no resource found exception behavior.
     * @param exception the exception value
     * @return the handle no resource found exception result
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(NoResourceFoundException exception) {
        return ResponseEntity
                .status(ErrorCode.NOT_FOUND.getStatus())
                .body(ApiResponse.error(ErrorCode.NOT_FOUND.getMessage()));
    }

    /**
     * Handles handle method not supported exception behavior.
     * @param exception the exception value
     * @return the handle method not supported exception result
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException exception) {
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error("지원하지 않는 HTTP 메서드입니다."));
    }

    /**
     * Handles handle unexpected exception behavior.
     * @param exception the exception value
     * @return the handle unexpected exception result
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }
}
