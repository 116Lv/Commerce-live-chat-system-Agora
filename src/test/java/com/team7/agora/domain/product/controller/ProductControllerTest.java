package com.team7.agora.domain.product.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.product.dto.request.ProductCreateRequest;
import com.team7.agora.domain.product.dto.response.ProductResponse;
import com.team7.agora.domain.product.enums.ProductStatus;
import com.team7.agora.domain.product.service.ProductService;
import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.response.ApiResponse;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Test
    void create_returnsCreatedProductEnvelope() {
        ProductController controller = new ProductController(productService);
        CustomUserDetails authUser = new CustomUserDetails(1L, "user@test.com", "password", UserRole.ROLE_USER, UserStatus.ACTIVE, "동네유저");
        ProductCreateRequest request = new ProductCreateRequest(
            "자전거",
            "상태 좋은 중고 자전거입니다.",
            BigDecimal.valueOf(73000),
            "SPORTS",
            1L
        );
        when(productService.create(1L, request)).thenReturn(
            new ProductResponse(1L, "자전거", "상태 좋은 중고 자전거입니다.", BigDecimal.valueOf(73000), "SPORTS", ProductStatus.SELLING)
        );

        ResponseEntity<ApiResponse<ProductResponse>> response = controller.create(authUser, request);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data().title()).isEqualTo("자전거");
    }
}
