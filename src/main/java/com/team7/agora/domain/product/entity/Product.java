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

    public boolean isSeller(Long userId) {
        return seller.getId() != null && seller.getId().equals(userId);
    }

    public void update(String title, String description, BigDecimal price, String category) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void markReserved() {
        this.status = ProductStatus.RESERVED;
    }

    public void restoreSelling() {
        this.status = ProductStatus.SELLING;
    }

    public void markSold() {
        this.status = ProductStatus.SOLD;
    }

    public void hide() {
        this.status = ProductStatus.HIDDEN;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        this.likeCount = Math.max(0, this.likeCount - 1);
    }
}
