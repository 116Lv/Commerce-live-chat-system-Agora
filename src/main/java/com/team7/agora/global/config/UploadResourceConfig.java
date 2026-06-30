package com.team7.agora.global.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class UploadResourceConfig implements WebMvcConfigurer {

    private static final String FILE_URI_PREFIX = "file:";

    private final String uploadLocation;

    public UploadResourceConfig(@Value("${file.upload-dir:uploads}") String uploadDir) {
        this.uploadLocation = Path.of(uploadDir).toAbsolutePath().normalize().toUri().toString();
        if (!this.uploadLocation.startsWith(FILE_URI_PREFIX)) {
            throw new IllegalArgumentException("Upload directory must resolve to a file: URI.");
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations(uploadLocation.endsWith("/") ? uploadLocation : uploadLocation + "/");
    }
}
