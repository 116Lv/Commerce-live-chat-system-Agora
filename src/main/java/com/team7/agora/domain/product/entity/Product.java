package com.team7.agora.domain.product.entity;

import com.team7.agora.domain.product.enums.ProductStatus;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "products",
    indexes = {
        @Index(name = "idx_products_status_deleted", columnList = "status, deleted_at"),
        @Index(name = "idx_products_region_status_deleted", columnList = "region_id, status, deleted_at"),
        @Index(name = "idx_products_category_status_deleted", columnList = "category, status, deleted_at"),
        @Index(name = "idx_products_title", columnList = "title")
    }
/**
 * JPA 엔티티이다.
 */
)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal price;

    @Column(nullable = false, length = 50)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @Column(nullable = false)
    private int viewCount;

    @Column(nullable = false)
    private int likeCount;

    private LocalDateTime deletedAt;

    private Product(User seller, Region region, String title, String description, BigDecimal price, String category) {
        this.seller = seller;
        this.region = region;
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.status = ProductStatus.SELLING;
        this.viewCount = 0;
        this.likeCount = 0;
    }

    /**
     * 도메인 객체를 생성한다.
     * @param seller 입력 값
     * @param region 입력 값
     * @param title 입력 값
     * @param description 입력 값
     * @param price 입력 값
     * @param category 입력 값
     * @return 처리 결과
     */
    public static Product create(
        User seller,
        Region region,
        String title,
        String description,
        BigDecimal price,
        String category
    ) {
        return new Product(seller, region, title, description, price, category);
    }

    /**
     * 조건 충족 여부를 확인한다.
     * @param userId 입력 값
     * @return 처리 결과
     */
    public boolean isSeller(Long userId) {
        return seller.getId() != null && seller.getId().equals(userId);
    }

    /**
     * 데이터를 수정한다.
     * @param title 입력 값
     * @param description 입력 값
     * @param price 입력 값
     * @param category 입력 값
     */
    public void update(String title, String description, BigDecimal price, String category) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
    }

    /**
     * 데이터를 삭제한다.
     */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 상태를 변경한다.
     */
    public void markReserved() {
        this.status = ProductStatus.RESERVED;
    }

    /**
     * 요청한 동작을 처리한다.
     */
    public void restoreSelling() {
        this.status = ProductStatus.SELLING;
    }

    /**
     * 상태를 변경한다.
     */
    public void markSold() {
        this.status = ProductStatus.SOLD;
    }

    /**
     * 요청한 동작을 처리한다.
     */
    public void hide() {
        this.status = ProductStatus.HIDDEN;
    }

    /**
     * 요청한 동작을 처리한다.
     */
    public void increaseLikeCount() {
        this.likeCount++;
    }

    /**
     * 요청한 동작을 처리한다.
     */
    public void decreaseLikeCount() {
        this.likeCount = Math.max(0, this.likeCount - 1);
    }
}
