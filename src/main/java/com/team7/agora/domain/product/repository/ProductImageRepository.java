package com.team7.agora.domain.product.repository;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository contract for storing and querying product image data.
 */
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    int countByProduct(Product product);
}
