package com.team7.agora.domain.product.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team7.agora.domain.product.entity.QProduct;
import com.team7.agora.domain.product.entity.QProductImage;
import com.team7.agora.domain.product.enums.ProductApprovalStatus;
import com.team7.agora.domain.product.enums.ProductStatus;
import com.team7.agora.domain.region.entity.QRegion;
import com.team7.agora.domain.search.dto.ProductSearchCondition;
import com.team7.agora.domain.search.dto.ProductSearchResponse;
import com.team7.agora.domain.user.entity.QUser;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

/**
 * 상품 검색 영속성 작업을 구현하는 저장소 어댑터이다.
 */
@Repository
public class ProductSearchRepositoryImpl implements ProductSearchRepository {

    private final JPAQueryFactory queryFactory;

    public ProductSearchRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 검색 조건과 정렬 조건에 맞는 상품 목록을 조회한다.
     * @param condition 검색 조건
     * @return 클라이언트에 반환할 API 응답
     */
    @Override
    public Page<ProductSearchResponse> search(ProductSearchCondition condition) {
        QProduct product = QProduct.product;
        QProductImage productImage = QProductImage.productImage;
        QRegion region = QRegion.region;
        QUser seller = QUser.user;

        BooleanBuilder where = new BooleanBuilder()
            .and(product.deletedAt.isNull())
            .and(product.status.ne(ProductStatus.HIDDEN))
            .and(product.approvalStatus.eq(ProductApprovalStatus.APPROVED));

        if (!condition.normalizedKeyword().isBlank()) {
            String pattern = "%" + condition.normalizedKeyword() + "%";
            where.and(product.title.lower().like(pattern).or(product.description.lower().like(pattern)));
        }
        if (condition.regionId() != null) {
            where.and(region.id.eq(condition.regionId()));
        }
        if (!condition.normalizedCategory().isBlank()) {
            where.and(product.category.eq(condition.normalizedCategory()));
        }

        List<ProductSearchResponse> content = queryFactory
            .select(Projections.constructor(
                ProductSearchResponse.class,
                product.id,
                product.title,
                product.price,
                region.id,
                region.name,
                region.sido,
                region.sigungu,
                region.eupmyeondong,
                product.category,
                product.status,
                product.likeCount,
                seller.id,
                seller.nickname,
                productImage.imageUrl
            ))
            .from(product)
            .join(product.region, region)
            .join(product.seller, seller)
            .leftJoin(productImage).on(productImage.product.eq(product).and(productImage.sortOrder.eq(0)))
            .where(where)
            .orderBy(product.id.desc())
            .offset(condition.pageable().getOffset())
            .limit(condition.pageable().getPageSize())
            .fetch();

        Long total = queryFactory
            .select(product.count())
            .from(product)
            .join(product.region, region)
            .where(where)
            .fetchOne();

        return new PageImpl<>(content, condition.pageable(), total == null ? 0 : total);
    }
}
