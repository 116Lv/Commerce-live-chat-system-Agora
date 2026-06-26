package com.team7.agora.global.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Storage contract for handling image storage files.
 */
public interface ImageStorageClient {

    String store(String category, MultipartFile file);
}
