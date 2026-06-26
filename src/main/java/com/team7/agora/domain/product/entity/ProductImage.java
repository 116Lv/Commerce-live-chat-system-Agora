// 상품 이미지 엔티티
package com.team7.agora.domain.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품 이미지 도메인 정보를 영속화하는 JPA 엔티티이다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "product_images",
    indexes = @Index(name = "idx_product_images_product", columnList = "product_id")
)
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private int sortOrder;

    private ProductImage(Product product, String imageUrl, int sortOrder) {
        this.product = product;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }

    /**
     * 상품에 연결할 이미지 URL과 표시 순서로 상품 이미지 엔티티를 생성한다.
     * @param product 상품 엔티티
     * @param imageUrl 저장된 이미지 접근 URL
     * @param sortOrder 이미지 표시 순서
     * @return 클라이언트에 반환할 API 응답
     */
    public static ProductImage create(Product product, String imageUrl, int sortOrder) {
        return new ProductImage(product, imageUrl, sortOrder);
    }
}
