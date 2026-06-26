package com.team7.agora.domain.coupon.entity;

import com.team7.agora.domain.common.entity.BaseTimeEntity;
import com.team7.agora.domain.coupon.enums.CouponEventStatus;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA 엔티티이다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "coupon_events")
public class CouponEvent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int totalQuantity;

    @Column(nullable = false)
    private int issuedQuantity;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CouponEventStatus status;

    private CouponEvent(String name, int totalQuantity, LocalDateTime startAt, LocalDateTime endAt) {
        markCreatedNow();
        this.name = name;
        this.totalQuantity = totalQuantity;
        this.issuedQuantity = 0;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = CouponEventStatus.ACTIVE;
    }

    /**
     * 도메인 객체를 생성한다.
     * @param name 입력 값
     * @param totalQuantity 입력 값
     * @param startAt 입력 값
     * @param endAt 입력 값
     * @return 처리 결과
     */
    public static CouponEvent create(String name, int totalQuantity, LocalDateTime startAt, LocalDateTime endAt) {
        return new CouponEvent(name, totalQuantity, startAt, endAt);
    }

    /**
     * 조건 충족 여부를 확인한다.
     */
    public void issue() {
        validateIssueable(LocalDateTime.now());
        this.issuedQuantity++;
    }

    /**
     * 규칙을 검증한다.
     * @param now 입력 값
     */
    public void validateIssueable(LocalDateTime now) {
        if (status != CouponEventStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.CONFLICT, "종료된 쿠폰 이벤트입니다.");
        }
        if (now.isBefore(startAt) || now.isAfter(endAt)) {
            throw new BusinessException(ErrorCode.CONFLICT, "쿠폰 이벤트 진행 시간이 아닙니다.");
        }
        if (issuedQuantity >= totalQuantity) {
            throw new BusinessException(ErrorCode.CONFLICT, "쿠폰이 모두 소진되었습니다.");
        }
    }
}
