package com.team7.agora.domain.product.repository;

import com.team7.agora.domain.search.dto.ProductSearchCondition;
import com.team7.agora.domain.search.dto.ProductSearchResponse;
import java.util.List;

public interface ProductSearchRepository {

    List<ProductSearchResponse> search(ProductSearchCondition condition);
}
