package com.team7.agora.domain.product.repository;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 상품 이미지 데이터 저장과 조회를 담당하는 저장소 인터페이스이다.
 */
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    int countByProduct(Product product);
}
