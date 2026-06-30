package com.team7.agora.domain.product.repository;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.entity.ProductLike;
import com.team7.agora.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 상품 좋아요 데이터 저장과 조회를 담당하는 저장소 인터페이스이다.
 */
public interface ProductLikeRepository extends JpaRepository<ProductLike, Long> {

    boolean existsByProductAndUser(Product product, User user);

    boolean existsByProductIdAndUserId(Long productId, Long userId);

    Optional<ProductLike> findByProductAndUser(Product product, User user);

    List<ProductLike> findAllByUser(User user);

    @Query("""
        select pl.product.id
        from ProductLike pl
        where pl.user.id = :userId
          and pl.product.id in :productIds
        """)
    List<Long> findLikedProductIdsByUserIdAndProductIdIn(
        @Param("userId") Long userId,
        @Param("productIds") List<Long> productIds
    );
}
