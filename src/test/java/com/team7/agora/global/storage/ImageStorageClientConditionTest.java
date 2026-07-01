package com.team7.agora.global.storage;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import software.amazon.awssdk.services.s3.S3Client;

class ImageStorageClientConditionTest {

    @Test
    void localStorageIsUsedWhenStorageTypeIsMissing() {
        new ApplicationContextRunner()
            .withUserConfiguration(StorageConditionTestConfig.class)
            .run(context -> {
                assertThat(context).hasSingleBean(LocalImageStorageClient.class);
                assertThat(context).doesNotHaveBean(S3ImageStorageClient.class);
            });
    }

    @Test
    void s3StorageIsUsedWhenStorageTypeIsS3() {
        new ApplicationContextRunner()
            .withPropertyValues(
                "storage.type=s3",
                "aws.s3.bucket=agora-product-images-986151953641",
                "aws.s3.region=ap-northeast-2",
                "aws.s3.public-base-url=https://cdn.example.com/agora"
            )
            .withUserConfiguration(StorageConditionTestConfig.class)
            .run(context -> {
                assertThat(context).hasSingleBean(S3ImageStorageClient.class);
                assertThat(context).doesNotHaveBean(LocalImageStorageClient.class);
            });
    }

    @Configuration
    @EnableConfigurationProperties(S3StorageProperties.class)
    @Import({LocalImageStorageClient.class, S3ImageStorageClient.class})
    static class StorageConditionTestConfig {

        @Bean
        S3Client s3Client() {
            return org.mockito.Mockito.mock(S3Client.class);
        }
    }
}
