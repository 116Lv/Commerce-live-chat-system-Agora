package com.team7.agora.domain.product.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.user.entity.User;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProductTest {

    @Test
    void createRecordsCreatedAtForDashboardMetrics() {
        Product product = Product.create(
                mock(User.class),
                mock(Region.class),
                "상품",
                "상품 설명",
                BigDecimal.valueOf(10000),
                "기타"
        );

        assertThat(product.getCreatedAt()).isNotNull();
    }
}
