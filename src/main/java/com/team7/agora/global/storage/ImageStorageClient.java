package com.team7.agora.global.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 이미지 저장 처리를 위한 계약이다.
 */
public interface ImageStorageClient {

    String store(String category, MultipartFile file);
}
