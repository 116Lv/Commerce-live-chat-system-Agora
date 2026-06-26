package com.team7.agora.domain.product.repository;

import com.team7.agora.domain.search.dto.ProductSearchCondition;
import com.team7.agora.domain.search.dto.ProductSearchResponse;
import java.util.List;

/**
 * 데이터 저장과 조회를 위한 저장소 계약이다.
 */
public interface ProductSearchRepository {

    List<ProductSearchResponse> search(ProductSearchCondition condition);
}
