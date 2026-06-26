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
 * JPA 엔티티이다.
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
     * 요청한 동작을 처리한다.
     * @param name 입력 값
     * @param discountAmount 입력 값
     * @param minOrderAmount 입력 값
     * @param validDays 입력 값
     * @return 처리 결과
     */
    public static Coupon firstCome(String name, int discountAmount, int minOrderAmount, int validDays) {
        return new Coupon(name, discountAmount, minOrderAmount, CouponType.FIRST_COME, validDays);
    }

    /**
     * 도메인 객체를 생성한다.
     * @param name 입력 값
     * @param discountAmount 입력 값
     * @param minOrderAmount 입력 값
     * @param type 입력 값
     * @param validDays 입력 값
     * @return 처리 결과
     */
    public static Coupon create(String name, int discountAmount, int minOrderAmount, CouponType type, int validDays) {
        return new Coupon(name, discountAmount, minOrderAmount, type, validDays);
    }
}
