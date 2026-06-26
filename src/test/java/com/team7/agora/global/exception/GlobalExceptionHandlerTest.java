package com.team7.agora.global.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.team7.agora.global.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @SuppressWarnings("deprecation")
    @Test
    void handleMaxUploadSizeExceededExceptionReturnsPayloadTooLarge() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleMaxUploadSizeExceededException(new MaxUploadSizeExceededException(10L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo("ERROR");
        assertThat(response.getBody().message()).isEqualTo("업로드 가능한 파일 크기를 초과했습니다.");
    }
}
