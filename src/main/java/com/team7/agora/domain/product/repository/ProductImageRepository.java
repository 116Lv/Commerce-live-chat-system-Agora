package com.team7.agora.domain.product.repository;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 데이터 저장과 조회를 위한 저장소 계약이다.
 */
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    int countByProduct(Product product);
}
