package com.team7.agora.domain.product.repository;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.entity.ProductLike;
import com.team7.agora.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository contract for storing and querying product like data.
 */
public interface ProductLikeRepository extends JpaRepository<ProductLike, Long> {

    boolean existsByProductAndUser(Product product, User user);

    Optional<ProductLike> findByProductAndUser(Product product, User user);

    List<ProductLike> findAllByUser(User user);
}
