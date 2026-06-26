package com.team7.agora.domain.product.repository;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.entity.ProductLike;
import com.team7.agora.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 데이터 저장과 조회를 위한 저장소 계약이다.
 */
public interface ProductLikeRepository extends JpaRepository<ProductLike, Long> {

    boolean existsByProductAndUser(Product product, User user);

    Optional<ProductLike> findByProductAndUser(Product product, User user);

    List<ProductLike> findAllByUser(User user);
}
