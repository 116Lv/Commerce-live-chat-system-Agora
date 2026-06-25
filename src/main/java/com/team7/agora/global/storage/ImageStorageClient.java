package com.team7.agora.global.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageClient {

    String store(String category, MultipartFile file);
}
