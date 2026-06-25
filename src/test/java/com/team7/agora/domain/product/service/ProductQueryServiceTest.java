package com.team7.agora.domain.product.service;
import static com.team7.agora.support.TestEntityIds.assignId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.product.dto.response.ProductResponse;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.enums.ProductStatus;
import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.region.entity.UserRegion;
import com.team7.agora.domain.region.repository.RegionRepository;
import com.team7.agora.domain.region.repository.UserRegionRepository;
import com.team7.agora.domain.search.service.ProductSearchService;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class ProductQueryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private UserRegionRepository userRegionRepository;

    @Mock
    private ProductSearchService productSearchService;

    @Test
    void getProductReturnsActiveProduct() {
        ProductService service = new ProductService(productRepository, userRepository, regionRepository, userRegionRepository, productSearchService);
        User seller = User.signup("seller@test.com", "password", "판매자", "01011112222");
        assignId(seller, 1L);
        Product product = Product.create(
            seller,
            Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동"),
            "자전거",
            "상태 좋아요",
            BigDecimal.valueOf(50000),
            "스포츠"
        );
        assignId(product, 10L);
        when(productRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(product));

        ProductResponse response = service.getProduct(10L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.title()).isEqualTo("자전거");
    }

    @Test
    void getProductsFiltersByExplicitRegionId() {
        ProductService service = new ProductService(productRepository, userRepository, regionRepository, userRegionRepository, productSearchService);
        User seller = User.signup("seller@test.com", "password", "판매자", "01011112222");
        assignId(seller, 1L);
        Product product = Product.create(
            seller,
            Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동"),
            "자전거", "상태 좋아요", BigDecimal.valueOf(50000), "스포츠"
        );
        assignId(product, 10L);
        when(productRepository.findAllByRegionIdInAndDeletedAtIsNullAndStatusNot(
            List.of(5L), ProductStatus.HIDDEN, PageRequest.of(0, 20)
        )).thenReturn(new PageImpl<>(List.of(product)));

        List<ProductResponse> responses = service.getProducts(null, 5L, PageRequest.of(0, 20));

        assertThat(responses).hasSize(1);
    }

    @Test
    void getProductsFallsBackToAllWhenAnonymousAndNoRegion() {
        ProductService service = new ProductService(productRepository, userRepository, regionRepository, userRegionRepository, productSearchService);
        User seller = User.signup("seller@test.com", "password", "판매자", "01011112222");
        assignId(seller, 1L);
        Product product = Product.create(
            seller,
            Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동"),
            "자전거", "상태 좋아요", BigDecimal.valueOf(50000), "스포츠"
        );
        assignId(product, 10L);
        when(productRepository.findAllByDeletedAtIsNullAndStatusNot(ProductStatus.HIDDEN, PageRequest.of(0, 20)))
            .thenReturn(new PageImpl<>(List.of(product)));

        List<ProductResponse> responses = service.getProducts(null, null, PageRequest.of(0, 20));

        assertThat(responses).hasSize(1);
    }

    @Test
    void getProductsUsesViewerPreferredRegionsWhenLoggedInAndNoRegionGiven() {
        ProductService service = new ProductService(productRepository, userRepository, regionRepository, userRegionRepository, productSearchService);
        User viewer = User.signup("viewer@test.com", "password", "조회자", "01022223333");
        assignId(viewer, 2L);
        Region region = Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동");
        assignId(region, 5L);
        UserRegion userRegion = UserRegion.of(viewer, region, true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(viewer));
        when(userRegionRepository.findAllByUser(viewer)).thenReturn(List.of(userRegion));
        when(productRepository.findAllByRegionIdInAndDeletedAtIsNullAndStatusNot(
            List.of(5L), ProductStatus.HIDDEN, PageRequest.of(0, 20)
        )).thenReturn(new PageImpl<>(List.of()));

        service.getProducts(2L, null, PageRequest.of(0, 20));

        org.mockito.Mockito.verify(productRepository).findAllByRegionIdInAndDeletedAtIsNullAndStatusNot(
            List.of(5L), ProductStatus.HIDDEN, PageRequest.of(0, 20)
        );
    }

    @Test
    void getMyProductsReturnsOnlySellerProducts() {
        ProductService service = new ProductService(productRepository, userRepository, regionRepository, userRegionRepository, productSearchService);
        User seller = User.signup("seller@test.com", "password", "판매자", "01011112222");
        assignId(seller, 1L);
        Product product = Product.create(
            seller,
            Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동"),
            "자전거", "상태 좋아요", BigDecimal.valueOf(50000), "스포츠"
        );
        assignId(product, 10L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(productRepository.findAllBySellerAndDeletedAtIsNull(seller)).thenReturn(List.of(product));

        List<ProductResponse> responses = service.getMyProducts(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).id()).isEqualTo(10L);
    }
}
