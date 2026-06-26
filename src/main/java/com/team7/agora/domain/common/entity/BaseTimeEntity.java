package com.team7.agora.domain.common.entity;

import com.team7.agora.global.time.AgoraClock;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;

/**
 * 엔티티 생성 시간을 저장하는 JPA 공통 상위 클래스이다.
 */
@Getter
@MappedSuperclass
public abstract class BaseTimeEntity {

    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 생성 시각을 현재 시각으로 설정한다.
     */
    protected void markCreatedNow() {
        this.createdAt = AgoraClock.now();
    }
}
