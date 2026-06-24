package com.team7.agora.domain.product.repository;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.search.dto.ProductSearchCondition;
import com.team7.agora.domain.search.dto.ProductSearchResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class ProductSearchRepositoryImpl implements ProductSearchRepository {

    private final EntityManager entityManager;

    public ProductSearchRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<ProductSearchResponse> search(ProductSearchCondition condition) {
        StringBuilder jpql = new StringBuilder("""
            select p
            from Product p
            join fetch p.region r
            where p.deletedAt is null
            and p.status <> com.team7.agora.domain.product.enums.ProductStatus.HIDDEN
            """);
        List<Object> parameters = new ArrayList<>();

        if (!condition.normalizedKeyword().isBlank()) {
            jpql.append(" and (lower(p.title) like ?1 or lower(p.description) like ?1)");
            parameters.add("%" + condition.normalizedKeyword() + "%");
        }

        if (condition.regionId() != null) {
            jpql.append(" and r.id = ?").append(parameters.size() + 1);
            parameters.add(condition.regionId());
        }

        if (condition.category() != null && !condition.category().isBlank()) {
            jpql.append(" and p.category = ?").append(parameters.size() + 1);
            parameters.add(condition.category());
        }

        jpql.append(" order by p.id desc");

        TypedQuery<Product> query = entityManager.createQuery(jpql.toString(), Product.class);
        for (int i = 0; i < parameters.size(); i++) {
            query.setParameter(i + 1, parameters.get(i));
        }
        query.setFirstResult((int) condition.pageable().getOffset());
        query.setMaxResults(condition.pageable().getPageSize());

        return query.getResultList().stream()
            .map(product -> new ProductSearchResponse(
                product.getId(),
                product.getTitle(),
                product.getPrice(),
                product.getRegion().getName()
            ))
            .toList();
    }
}
