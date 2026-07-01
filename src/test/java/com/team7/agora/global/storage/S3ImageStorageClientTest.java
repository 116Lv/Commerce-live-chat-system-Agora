package com.team7.agora.global.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

class S3ImageStorageClientTest {

    private final S3Client s3Client = org.mockito.Mockito.mock(S3Client.class);
    private final S3StorageProperties properties = new S3StorageProperties(
        "agora-product-images-986151953641",
        "ap-northeast-2",
        "https://cdn.example.com/agora"
    );

    @Test
    void storeUploadsImageToS3AndReturnsPublicUrl() {
        S3ImageStorageClient storageClient = new S3ImageStorageClient(s3Client, properties);
        MockMultipartFile file = new MockMultipartFile(
            "images",
            "helmet.png",
            "image/png",
            "image".getBytes(StandardCharsets.UTF_8)
        );

        String imageUrl = storageClient.store("products", file);

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
        PutObjectRequest request = requestCaptor.getValue();
        assertThat(request.bucket()).isEqualTo("agora-product-images-986151953641");
        assertThat(request.key()).startsWith("products/");
        assertThat(request.key()).endsWith(".png");
        assertThat(request.contentType()).isEqualTo("image/png");
        assertThat(imageUrl).isEqualTo("https://cdn.example.com/agora/" + request.key());
    }

    @Test
    void storeRejectsUnsupportedCategory() {
        S3ImageStorageClient storageClient = new S3ImageStorageClient(s3Client, properties);
        MockMultipartFile file = new MockMultipartFile("images", "helmet.png", "image/png", "image".getBytes());

        assertThatThrownBy(() -> storageClient.store("../secret", file))
            .isInstanceOf(com.team7.agora.global.exception.BusinessException.class);
    }
}
