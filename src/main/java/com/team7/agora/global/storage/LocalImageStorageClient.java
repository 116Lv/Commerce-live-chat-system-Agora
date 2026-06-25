package com.team7.agora.global.storage;

import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Component
public class LocalImageStorageClient implements ImageStorageClient {

    private final Path baseUploadDir;

    public LocalImageStorageClient(@Value("${file.upload-dir:uploads}") String baseUploadDir) {
        this.baseUploadDir = Path.of(baseUploadDir);
    }

    @Override
    public String store(String category, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "업로드할 이미지가 없습니다.");
        }

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
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
}
