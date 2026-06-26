package com.team7.agora.domain.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;

/**
 * Base JPA superclass that stores entity creation timestamps.
 */
@Getter
@MappedSuperclass
public abstract class BaseTimeEntity {

    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Updates the creation timestamp to the current time.
     */
    protected void markCreatedNow() {
        this.createdAt = LocalDateTime.now();
    }
}