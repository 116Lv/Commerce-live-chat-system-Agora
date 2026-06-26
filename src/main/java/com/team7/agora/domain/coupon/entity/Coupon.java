package com.team7.agora.domain.coupon.entity;

import com.team7.agora.domain.coupon.enums.CouponStatus;
import com.team7.agora.domain.coupon.enums.CouponType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Coupon 도메인 정보를 영속화하는 JPA 엔티티이다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int discountAmount;

    @Column(nullable = false)
    private int minOrderAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CouponType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CouponStatus status;

    @Column(nullable = false)
    private int validDays;

    private Coupon(String name, int discountAmount, int minOrderAmount, CouponType type, int validDays) {
        this.name = name;
        this.discountAmount = discountAmount;
        this.minOrderAmount = minOrderAmount;
        this.type = type;
        this.status = CouponStatus.ACTIVE;
        this.validDays = validDays;
    }

    /**
     * 'firstCome' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     * @param name 이름 또는 제목
     * @param discountAmount 쿠폰 할인 금액
     * @param minOrderAmount 쿠폰 사용을 위한 최소 주문 금액
     * @param validDays 쿠폰 유효 일수
     * @return 클라이언트에 반환할 API 응답
     */
    public static Coupon firstCome(String name, int discountAmount, int minOrderAmount, int validDays) {
        return new Coupon(name, discountAmount, minOrderAmount, CouponType.FIRST_COME, validDays);
    }

    /**
     * 관리자가 입력한 쿠폰 정책으로 새 쿠폰 엔티티를 생성한다.
     * @param name 이름 또는 제목
     * @param discountAmount 쿠폰 할인 금액
     * @param minOrderAmount 쿠폰 사용을 위한 최소 주문 금액
     * @param type 쿠폰 유형
     * @param validDays 쿠폰 유효 일수
     * @return 클라이언트에 반환할 API 응답
     */
    public static Coupon create(String name, int discountAmount, int minOrderAmount, CouponType type, int validDays) {
        return new Coupon(name, discountAmount, minOrderAmount, type, validDays);
    }
}
