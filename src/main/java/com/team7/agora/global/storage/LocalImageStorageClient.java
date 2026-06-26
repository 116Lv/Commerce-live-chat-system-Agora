package com.team7.agora.global.storage;

import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Component
public class LocalImageStorageClient implements ImageStorageClient {

    private static final Set<String> ALLOWED_CATEGORIES = Set.of("products", "chat");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "image/jpeg",
        "image/png",
        "image/webp",
        "image/gif"
    );

    private final Path baseUploadDir;

    public LocalImageStorageClient(@Value("${file.upload-dir:uploads}") String baseUploadDir) {
        this.baseUploadDir = Path.of(baseUploadDir);
    }

    @Override
    public String store(String category, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "업로드할 이미지가 없습니다.");
        }
        validateCategory(category);
        validateContentType(file);

        String extension = validateAndNormalizeExtension(file);
        String filename = UUID.randomUUID() + (extension != null ? "." + extension : "");
        Path categoryDir = baseUploadDir.resolve(category);

        try {
            Files.createDirectories(categoryDir);
            file.transferTo(categoryDir.resolve(filename));
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "이미지 저장에 실패했습니다.");
        }

        return "/uploads/" + category + "/" + filename;
    }

    private void validateCategory(String category) {
        if (!ALLOWED_CATEGORIES.contains(category)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "지원하지 않는 업로드 경로입니다.");
        }
    }

    private void validateContentType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "지원하지 않는 이미지 형식입니다.");
        }
    }

    private String validateAndNormalizeExtension(MultipartFile file) {
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (extension == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "이미지 파일 확장자가 필요합니다.");
        }

        String normalizedExtension = extension.toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(normalizedExtension)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "지원하지 않는 이미지 확장자입니다.");
        }
        return normalizedExtension;
    }
}
