package com.team7.agora.domain.product.entity;

import com.team7.agora.domain.product.enums.ProductStatus;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.global.time.AgoraClock;
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
 * 상품 도메인 정보를 영속화하는 JPA 엔티티이다.
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
     * 판매자, 지역, 상품 제목과 가격 정보로 새 상품 엔티티를 생성한다.
     * @param seller 판매자 엔티티
     * @param region 거래 지역 엔티티
     * @param title 상품 제목 또는 화면에 표시할 제목
     * @param description 상품 설명 또는 상세 내용
     * @param price 가격
     * @param category 업로드 카테고리
     * @return 클라이언트에 반환할 API 응답
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
     * @param userId 회원 ID
     * @return 클라이언트에 반환할 API 응답
     */
    public boolean isSeller(Long userId) {
        return seller.getId() != null && seller.getId().equals(userId);
    }

    /**
     * 데이터를 수정한다.
     * @param title 상품 제목 또는 화면에 표시할 제목
     * @param description 상품 설명 또는 상세 내용
     * @param price 가격
     * @param category 업로드 카테고리
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
        this.deletedAt = AgoraClock.now();
    }

    /**
     * 예약 가능한 상품 또는 거래를 예약 상태로 변경한다.
     */
    public void markReserved() {
        this.status = ProductStatus.RESERVED;
    }

    /**
     * 관리자가 숨겨진 상품을 다시 판매 중 상태로 되돌린다.
     */
    public void restoreSelling() {
        this.status = ProductStatus.SELLING;
    }

    /**
     * 상품 또는 거래를 판매 완료 상태로 변경한다.
     */
    public void markSold() {
        this.status = ProductStatus.SOLD;
    }

    /**
     * 'hide' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     */
    public void hide() {
        this.status = ProductStatus.HIDDEN;
    }

    /**
     * 상품 좋아요 수를 1 증가시킨다.
     */
    public void increaseLikeCount() {
        this.likeCount++;
    }

    /**
     * 상품 좋아요 수를 1 감소시킨다.
     */
    public void decreaseLikeCount() {
        this.likeCount = Math.max(0, this.likeCount - 1);
    }
}
