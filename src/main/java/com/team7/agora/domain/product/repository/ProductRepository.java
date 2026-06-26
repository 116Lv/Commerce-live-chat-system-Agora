package com.team7.agora.domain.product.repository;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.enums.ProductStatus;
import com.team7.agora.domain.search.dto.ProductSearchCondition;
import com.team7.agora.domain.search.dto.ProductSearchResponse;
import com.team7.agora.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 상품 데이터 저장과 조회를 담당하는 저장소 인터페이스이다.
 */
public interface ProductRepository extends JpaRepository<Product, Long>, ProductSearchRepository {

    Optional<Product> findByIdAndDeletedAtIsNull(Long id);

    Page<Product> findAllByDeletedAtIsNullAndStatusNot(ProductStatus status, Pageable pageable);

    Page<Product> findAllByRegionIdInAndDeletedAtIsNullAndStatusNot(List<Long> regionIds, ProductStatus status, Pageable pageable);

    List<Product> findAllBySellerAndDeletedAtIsNull(User seller);
}
