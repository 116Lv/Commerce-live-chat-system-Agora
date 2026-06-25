package com.team7.agora.domain.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class BaseTimeEntity {

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected void markCreatedNow() {
        this.createdAt = LocalDateTime.now();
    }
}