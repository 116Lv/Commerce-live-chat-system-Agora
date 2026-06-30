package com.team7.agora.domain.product.repository;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.enums.ProductApprovalStatus;
import com.team7.agora.domain.product.enums.ProductStatus;
import com.team7.agora.domain.search.dto.ProductSearchCondition;
import com.team7.agora.domain.search.dto.ProductSearchResponse;
import com.team7.agora.domain.user.entity.User;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 상품 데이터 저장과 조회를 담당하는 저장소 인터페이스이다.
 */
public interface ProductRepository extends JpaRepository<Product, Long>, ProductSearchRepository {

    Optional<Product> findByIdAndDeletedAtIsNull(Long id);

    Optional<Product> findByIdAndDeletedAtIsNullAndApprovalStatus(Long id, ProductApprovalStatus approvalStatus);

    boolean existsByIdAndSellerIdAndDeletedAtIsNull(Long id, Long sellerId);

    @EntityGraph(attributePaths = {"seller", "region"})
    @Query("select p from Product p where p.id = :id and p.deletedAt is null")
    Optional<Product> findWithSellerAndRegionByIdAndDeletedAtIsNull(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id and p.deletedAt is null")
    Optional<Product> findByIdForUpdateAndDeletedAtIsNull(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select p from Product p
        where p.id = :id
          and p.deletedAt is null
          and p.approvalStatus = :approvalStatus
        """)
    Optional<Product> findByIdForUpdateAndDeletedAtIsNullAndApprovalStatus(
        @Param("id") Long id,
        @Param("approvalStatus") ProductApprovalStatus approvalStatus
    );

    @EntityGraph(attributePaths = {"seller", "region"})
    Page<Product> findAllByDeletedAtIsNullAndStatusNot(ProductStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"seller", "region"})
    Page<Product> findAllByDeletedAtIsNullAndStatusNotAndApprovalStatus(
        ProductStatus status,
        ProductApprovalStatus approvalStatus,
        Pageable pageable
    );

    @EntityGraph(attributePaths = {"seller", "region"})
    Page<Product> findAllByStatus(ProductStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"seller", "region"})
    Page<Product> findAllByApprovalStatus(ProductApprovalStatus approvalStatus, Pageable pageable);

    @EntityGraph(attributePaths = {"seller", "region"})
    @Query("""
        select p from Product p
        join p.seller seller
        where (:keyword is null or lower(p.title) like lower(concat('%', :keyword, '%')))
          and (:sellerKeyword is null or lower(seller.nickname) like lower(concat('%', :sellerKeyword, '%')))
          and (:status is null or p.status = :status)
          and (:approvalStatus is null or p.approvalStatus = :approvalStatus)
        """)
    Page<Product> findAdminProducts(
        @Param("keyword") String keyword,
        @Param("sellerKeyword") String sellerKeyword,
        @Param("status") ProductStatus status,
        @Param("approvalStatus") ProductApprovalStatus approvalStatus,
        Pageable pageable
    );

    @EntityGraph(attributePaths = {"seller", "region"})
    Page<Product> findAllByStatusNotIn(List<ProductStatus> statuses, Pageable pageable);

    @EntityGraph(attributePaths = {"seller", "region"})
    Page<Product> findAllByRegionIdInAndDeletedAtIsNullAndStatusNot(List<Long> regionIds, ProductStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"seller", "region"})
    Page<Product> findAllByRegionIdInAndDeletedAtIsNullAndStatusNotAndApprovalStatus(
        List<Long> regionIds,
        ProductStatus status,
        ProductApprovalStatus approvalStatus,
        Pageable pageable
    );

    @EntityGraph(attributePaths = {"seller", "region"})
    List<Product> findAllBySellerAndDeletedAtIsNull(User seller);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Product p set p.likeCount = p.likeCount + 1 where p.id = :productId")
    int increaseLikeCount(@Param("productId") Long productId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Product p set p.likeCount = case when p.likeCount > 0 then p.likeCount - 1 else 0 end where p.id = :productId")
    int decreaseLikeCount(@Param("productId") Long productId);

    @Query("select p.likeCount from Product p where p.id = :productId")
    int findLikeCountById(@Param("productId") Long productId);
}
