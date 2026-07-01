package com.team7.agora.global.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "aws.s3")
public record S3StorageProperties(
    String bucket,
    String region,
    String publicBaseUrl
) {

    public String resolvedPublicBaseUrl() {
        if (StringUtils.hasText(publicBaseUrl)) {
            return trimTrailingSlash(publicBaseUrl);
        }
        return "https://" + bucket + ".s3." + region + ".amazonaws.com";
    }

    private String trimTrailingSlash(String value) {
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
