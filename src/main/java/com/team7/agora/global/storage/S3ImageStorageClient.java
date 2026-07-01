package com.team7.agora.global.storage;

import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class S3ImageStorageClient implements ImageStorageClient {

    private static final Set<String> ALLOWED_CATEGORIES = Set.of("products", "chat");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "image/jpeg",
        "image/png",
        "image/webp",
        "image/gif"
    );

    private final S3Client s3Client;
    private final S3StorageProperties properties;

    public S3ImageStorageClient(S3Client s3Client, S3StorageProperties properties) {
        this.s3Client = s3Client;
        this.properties = properties;
    }

    @Override
    public String store(String category, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "?낅줈?쒗븷 ?대?吏媛 ?놁뒿?덈떎.");
        }
        validateRequiredProperties();
        validateCategory(category);
        validateContentType(file);

        String extension = validateAndNormalizeExtension(file);
        String key = category + "/" + UUID.randomUUID() + "." + extension;
        PutObjectRequest request = PutObjectRequest.builder()
            .bucket(properties.bucket())
            .key(key)
            .contentType(file.getContentType())
            .contentLength(file.getSize())
            .build();

        try (InputStream inputStream = file.getInputStream()) {
            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, file.getSize()));
        } catch (IOException | S3Exception | SdkClientException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "?대?吏 ??μ뿉 ?ㅽ뙣?덉뒿?덈떎.");
        }

        return properties.resolvedPublicBaseUrl() + "/" + key;
    }

    private void validateRequiredProperties() {
        if (!StringUtils.hasText(properties.bucket()) || !StringUtils.hasText(properties.region())) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "?대?吏 ??μ뿉 ?ㅽ뙣?덉뒿?덈떎.");
        }
    }

    private void validateCategory(String category) {
        if (!ALLOWED_CATEGORIES.contains(category)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "吏?먰븯吏 ?딅뒗 ?낅줈??寃쎈줈?낅땲??");
        }
    }

    private void validateContentType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "吏?먰븯吏 ?딅뒗 ?대?吏 ?뺤떇?낅땲??");
        }
    }

    private String validateAndNormalizeExtension(MultipartFile file) {
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (extension == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "?대?吏 ?뚯씪 ?뺤옣?먭? ?꾩슂?⑸땲??");
        }

        String normalizedExtension = extension.toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(normalizedExtension)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "吏?먰븯吏 ?딅뒗 ?대?吏 ?뺤옣?먯엯?덈떎.");
        }
        return normalizedExtension;
    }
}
