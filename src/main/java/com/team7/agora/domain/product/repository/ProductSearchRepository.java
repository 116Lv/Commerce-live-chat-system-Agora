package com.team7.agora.domain.product.repository;

import com.team7.agora.domain.search.dto.ProductSearchCondition;
import com.team7.agora.domain.search.dto.ProductSearchResponse;
import org.springframework.data.domain.Page;

/**
 * 상품 검색 데이터 저장과 조회를 담당하는 저장소 인터페이스이다.
 */
public interface ProductSearchRepository {

    Page<ProductSearchResponse> search(ProductSearchCondition condition);
}
