// 상품 찜 엔티티
package com.team7.agora.domain.product.entity;

import com.team7.agora.domain.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품 좋아요 도메인 정보를 영속화하는 JPA 엔티티이다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "product_likes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "user_id"})
)
public class ProductLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private ProductLike(Product product, User user) {
        this.product = product;
        this.user = user;
    }

    /**
     * 회원과 상품 정보를 묶어 새 상품 좋아요 엔티티를 생성한다.
     * @param product 상품 엔티티
     * @param user 회원 엔티티
     * @return 클라이언트에 반환할 API 응답
     */
    public static ProductLike create(Product product, User user) {
        return new ProductLike(product, user);
    }
}
