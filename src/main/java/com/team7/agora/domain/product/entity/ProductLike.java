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
 * JPA 엔티티이다.
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
     * 도메인 객체를 생성한다.
     * @param product 입력 값
     * @param user 입력 값
     * @return 처리 결과
     */
    public static ProductLike create(Product product, User user) {
        return new ProductLike(product, user);
    }
}
