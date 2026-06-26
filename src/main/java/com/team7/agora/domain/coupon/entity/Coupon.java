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
 * JPA entity that represents a coupon record.
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
     * Handles first come behavior.
     * @param name the name value
     * @param discountAmount the discount amount value
     * @param minOrderAmount the min order amount value
     * @param validDays the valid days value
     * @return the first come result
     */
    public static Coupon firstCome(String name, int discountAmount, int minOrderAmount, int validDays) {
        return new Coupon(name, discountAmount, minOrderAmount, CouponType.FIRST_COME, validDays);
    }

    /**
     * Creates create data.
     * @param name the name value
     * @param discountAmount the discount amount value
     * @param minOrderAmount the min order amount value
     * @param type the type value
     * @param validDays the valid days value
     * @return the create result
     */
    public static Coupon create(String name, int discountAmount, int minOrderAmount, CouponType type, int validDays) {
        return new Coupon(name, discountAmount, minOrderAmount, type, validDays);
    }
}
