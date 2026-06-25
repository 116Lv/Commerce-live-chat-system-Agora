package com.team7.agora.domain.search.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.search.dto.ProductSearchResponse;
import com.team7.agora.domain.search.service.ProductSearchService;
import com.team7.agora.global.response.ApiResponse;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    @Mock
    private ProductSearchService productSearchService;

    @Test
    void searchV1_returnsSearchResult() {
        SearchController controller = new SearchController(productSearchService);
        when(productSearchService.searchV1(any())).thenReturn(List.of(
            new ProductSearchResponse(1L, "자전거", BigDecimal.valueOf(73000), "서울 강남구 역삼동")
        ));

        ResponseEntity<ApiResponse<List<ProductSearchResponse>>> response =
            controller.searchV1("자전거", null, null, 0, 20);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).hasSize(1);
    }

    @Test
    void searchV1_withNoKeyword_returnsAllResults() {
        SearchController controller = new SearchController(productSearchService);
        when(productSearchService.searchV1(any())).thenReturn(List.of(
            new ProductSearchResponse(1L, "자전거", BigDecimal.valueOf(73000), "서울 강남구 역삼동"),
            new ProductSearchResponse(2L, "노트북", BigDecimal.valueOf(500000), "서울 마포구 합정동")
        ));

        ResponseEntity<ApiResponse<List<ProductSearchResponse>>> response =
            controller.searchV1(null, null, null, 0, 20);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).hasSize(2);
    }
}
