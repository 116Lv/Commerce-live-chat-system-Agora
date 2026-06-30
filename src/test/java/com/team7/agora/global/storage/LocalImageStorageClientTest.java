// 로컬 이미지 저장소의 업로드 입력 검증을 확인하는 테스트
package com.team7.agora.global.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class LocalImageStorageClientTest {

    @TempDir
    private Path uploadDir;

    @Test
    void storeSavesImageAndReturnsPublicUploadUrl() throws Exception {
        LocalImageStorageClient storageClient = new LocalImageStorageClient(uploadDir.toString());
        MockMultipartFile file = image("제일택배 영수증.jpg", "image/jpeg");

        String imageUrl = storageClient.store("products", file);

        assertThat(imageUrl).startsWith("/uploads/products/");
        assertThat(imageUrl).endsWith(".jpg");
        assertThat(Files.exists(uploadDir.resolve("products").resolve(Path.of(imageUrl).getFileName()))).isTrue();
    }

    @Test
    void storeRejectsUnsupportedCategory() {
        LocalImageStorageClient storageClient = new LocalImageStorageClient(uploadDir.toString());
        MockMultipartFile file = image("image.jpg", "image/jpeg");

        assertThatThrownBy(() -> storageClient.store("../secret", file))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    void storeRejectsUnsupportedExtension() {
        LocalImageStorageClient storageClient = new LocalImageStorageClient(uploadDir.toString());
        MockMultipartFile file = image("image.exe", "image/jpeg");

        assertThatThrownBy(() -> storageClient.store("products", file))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    void storeRejectsUnsupportedContentType() {
        LocalImageStorageClient storageClient = new LocalImageStorageClient(uploadDir.toString());
        MockMultipartFile file = image("image.jpg", "text/plain");

        assertThatThrownBy(() -> storageClient.store("products", file))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    private MockMultipartFile image(String filename, String contentType) {
        return new MockMultipartFile("file", filename, contentType, "image".getBytes());
    }
}
