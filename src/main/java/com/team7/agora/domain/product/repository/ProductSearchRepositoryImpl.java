package com.team7.agora.domain.product.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team7.agora.domain.product.entity.QProduct;
import com.team7.agora.domain.product.enums.ProductStatus;
import com.team7.agora.domain.region.entity.QRegion;
import com.team7.agora.domain.search.dto.ProductSearchCondition;
import com.team7.agora.domain.search.dto.ProductSearchResponse;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * 영속성 작업을 구현하는 저장소 어댑터이다.
 */
@Repository
public class ProductSearchRepositoryImpl implements ProductSearchRepository {

    private final JPAQueryFactory queryFactory;

    public ProductSearchRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 요청한 동작을 처리한다.
     * @param condition 입력 값
     * @return 처리 결과
     */
    @Override
    public List<ProductSearchResponse> search(ProductSearchCondition condition) {
        QProduct product = QProduct.product;
        QRegion region = QRegion.region;

        BooleanBuilder where = new BooleanBuilder()
            .and(product.deletedAt.isNull())
            .and(product.status.ne(ProductStatus.HIDDEN));

        if (!condition.normalizedKeyword().isBlank()) {
            String pattern = "%" + condition.normalizedKeyword() + "%";
            where.and(product.title.lower().like(pattern).or(product.description.lower().like(pattern)));
        }
        if (condition.regionId() != null) {
            where.and(region.id.eq(condition.regionId()));
        }
        if (condition.category() != null && !condition.category().isBlank()) {
            where.and(product.category.eq(condition.category()));
        }

        return queryFactory
            .select(Projections.constructor(
                ProductSearchResponse.class,
                product.id, product.title, product.price, region.name
            ))
            .from(product)
            .join(product.region, region)
            .where(where)
            .orderBy(product.id.desc())
            .offset(condition.pageable().getOffset())
            .limit(condition.pageable().getPageSize())
            .fetch();
    }
}
