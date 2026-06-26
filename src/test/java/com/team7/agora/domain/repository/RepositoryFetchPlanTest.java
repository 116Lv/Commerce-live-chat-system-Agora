package com.team7.agora.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.team7.agora.domain.chat.repository.ChatRoomRepository;
import com.team7.agora.domain.product.enums.ProductStatus;
import com.team7.agora.domain.product.repository.ProductRepository;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;

class RepositoryFetchPlanTest {

    @Test
    void productListQueriesFetchSellerAndRegion() throws NoSuchMethodException {
        assertEntityGraph(
            ProductRepository.class.getMethod(
                "findAllByDeletedAtIsNullAndStatusNot",
                ProductStatus.class,
                Pageable.class
            ),
            "seller",
            "region"
        );
        assertEntityGraph(
            ProductRepository.class.getMethod(
                "findAllByRegionIdInAndDeletedAtIsNullAndStatusNot",
                List.class,
                ProductStatus.class,
                Pageable.class
            ),
            "seller",
            "region"
        );
        assertEntityGraph(
            ProductRepository.class.getMethod(
                "findAllBySellerAndDeletedAtIsNull",
                com.team7.agora.domain.user.entity.User.class
            ),
            "seller",
            "region"
        );
    }

    @Test
    void chatRoomListQueryFetchesParticipantsAndProduct() throws NoSuchMethodException {
        assertEntityGraph(
            ChatRoomRepository.class.getMethod(
                "findAllBySellerOrBuyer",
                com.team7.agora.domain.user.entity.User.class,
                com.team7.agora.domain.user.entity.User.class
            ),
            "seller",
            "buyer",
            "product"
        );
    }

    private void assertEntityGraph(Method method, String... attributePaths) {
        EntityGraph entityGraph = method.getAnnotation(EntityGraph.class);
        assertThat(entityGraph).isNotNull();
        assertThat(entityGraph.attributePaths()).containsExactlyInAnyOrder(attributePaths);
    }
}
