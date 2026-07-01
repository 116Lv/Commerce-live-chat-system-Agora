package com.team7.agora.domain.product.service;
import static com.team7.agora.support.TestEntityIds.assignId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.product.dto.response.ProductResponse;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.entity.ProductImage;
import com.team7.agora.domain.product.enums.ProductApprovalStatus;
import com.team7.agora.domain.product.enums.ProductStatus;
import com.team7.agora.domain.product.repository.ProductImageRepository;
import com.team7.agora.domain.product.repository.ProductLikeRepository;
import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.region.entity.UserRegion;
import com.team7.agora.domain.region.repository.RegionRepository;
import com.team7.agora.domain.region.repository.UserRegionRepository;
import com.team7.agora.domain.search.service.ProductSearchService;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
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

    @Mock
    private ProductLikeRepository productLikeRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    private ProductService newService() {
        return new ProductService(
            productRepository,
            userRepository,
            regionRepository,
            userRegionRepository,
            productSearchService,
            productLikeRepository,
            productImageRepository
        );
    }

    @Test
    void getProductReturnsActiveProduct() {
        ProductService service = newService();
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
        product.approve();
        when(productRepository.findWithSellerAndRegionByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(product));

        ProductResponse response = service.getProduct(10L);

        assertThat(response.productId()).isEqualTo(10L);
        assertThat(response.title()).isEqualTo("자전거");
    }

    @Test
    void getProductsFiltersByExplicitRegionId() {
        ProductService service = newService();
        User seller = User.signup("seller@test.com", "password", "판매자", "01011112222");
        assignId(seller, 1L);
        Product product = Product.create(
            seller,
            Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동"),
            "자전거", "상태 좋아요", BigDecimal.valueOf(50000), "스포츠"
        );
        assignId(product, 10L);
        when(productRepository.findAllByRegionIdInAndDeletedAtIsNullAndStatusNotAndApprovalStatus(
            List.of(5L), ProductStatus.HIDDEN, ProductApprovalStatus.APPROVED, PageRequest.of(0, 20)
        )).thenReturn(new PageImpl<>(List.of(product)));

        List<ProductResponse> responses = service.getProducts(null, 5L, PageRequest.of(0, 20));

        assertThat(responses).hasSize(1);
    }

    @Test
    void getProductsFallsBackToAllWhenAnonymousAndNoRegion() {
        ProductService service = newService();
        User seller = User.signup("seller@test.com", "password", "판매자", "01011112222");
        assignId(seller, 1L);
        Product product = Product.create(
            seller,
            Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동"),
            "자전거", "상태 좋아요", BigDecimal.valueOf(50000), "스포츠"
        );
        assignId(product, 10L);
        when(productRepository.findAllByDeletedAtIsNullAndStatusNotAndApprovalStatus(
            ProductStatus.HIDDEN, ProductApprovalStatus.APPROVED, PageRequest.of(0, 20)
        ))
            .thenReturn(new PageImpl<>(List.of(product)));

        List<ProductResponse> responses = service.getProducts(null, null, PageRequest.of(0, 20));

        assertThat(responses).hasSize(1);
    }

    @Test
    void getProductsFallsBackToAllWhenLoggedInAndNoRegionGiven() {
        ProductService service = newService();
        User viewer = User.signup("viewer@test.com", "password", "조회자", "01022223333");
        assignId(viewer, 2L);
        Region region = Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동");
        assignId(region, 5L);
        UserRegion userRegion = UserRegion.of(viewer, region, true);
        org.mockito.Mockito.lenient().when(userRepository.findById(2L)).thenReturn(Optional.of(viewer));
        org.mockito.Mockito.lenient().when(userRegionRepository.findAllByUser(viewer)).thenReturn(List.of(userRegion));
        org.mockito.Mockito.lenient().when(productRepository.findAllByRegionIdInAndDeletedAtIsNullAndStatusNotAndApprovalStatus(
            List.of(5L), ProductStatus.HIDDEN, ProductApprovalStatus.APPROVED, PageRequest.of(0, 20)
        )).thenReturn(new PageImpl<>(List.of()));
        when(productRepository.findAllByDeletedAtIsNullAndStatusNotAndApprovalStatus(
            ProductStatus.HIDDEN, ProductApprovalStatus.APPROVED, PageRequest.of(0, 20)
        )).thenReturn(new PageImpl<>(List.of()));

        service.getProducts(2L, null, PageRequest.of(0, 20));

        org.mockito.Mockito.verify(productRepository).findAllByDeletedAtIsNullAndStatusNotAndApprovalStatus(
            ProductStatus.HIDDEN, ProductApprovalStatus.APPROVED, PageRequest.of(0, 20)
        );
    }

    @Test
    void getMyProductsReturnsOnlySellerProducts() {
        ProductService service = newService();
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
        assertThat(responses.get(0).productId()).isEqualTo(10L);
    }

    @Test
    void getProductRejectsPendingProductForPublicViewer() {
        ProductService service = newService();
        User seller = User.signup("seller@test.com", "password", "seller", "01011112222");
        assignId(seller, 1L);
        Product product = Product.create(
            seller,
            Region.create("Seoul Gangnam", "1168010100", "Seoul", "Gangnam", "Samseong"),
            "Pending bike",
            "Waiting approval",
            BigDecimal.valueOf(50000),
            "SPORTS"
        );
        assignId(product, 10L);
        when(productRepository.findWithSellerAndRegionByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> service.getProduct(null, 10L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void getProductIncludesViewerLikedAndMetadata() {
        ProductService service = newService();
        User seller = User.signup("seller@test.com", "password", "seller", "01011112222");
        assignId(seller, 1L);
        Region region = Region.create("Seoul Gangnam", "1168010100", "Seoul", "Gangnam", "Samseong");
        assignId(region, 5L);
        Product product = Product.create(
            seller,
            region,
            "Bike",
            "Good bike",
            BigDecimal.valueOf(50000),
            "SPORTS"
        );
        assignId(product, 10L);
        product.approve();
        product.increaseLikeCount();
        ProductImage image = ProductImage.create(product, "https://cdn.test/products/10-main.jpg", 0);
        assignId(image, 100L);
        ProductImage detailImage = ProductImage.create(product, "https://cdn.test/products/10-detail.jpg", 1);
        assignId(detailImage, 101L);
        when(productRepository.findWithSellerAndRegionByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(product));
        when(productLikeRepository.existsByProductIdAndUserId(10L, 2L)).thenReturn(true);
        when(productImageRepository.findAllByProductIdInOrderByProductIdAscSortOrderAsc(List.of(10L)))
            .thenReturn(List.of(image, detailImage));

        ProductResponse response = service.getProduct(2L, 10L);

        assertThat(response.productId()).isEqualTo(10L);
        assertThat(response.likeCount()).isEqualTo(1);
        assertThat(response.liked()).isTrue();
        assertThat(response.regionId()).isEqualTo(5L);
        assertThat(response.regionFullName()).isEqualTo("Seoul Gangnam");
        assertThat(response.sido()).isEqualTo("Seoul");
        assertThat(response.sigungu()).isEqualTo("Gangnam");
        assertThat(response.eupmyeondong()).isEqualTo("Samseong");
        assertThat(response.sellerId()).isEqualTo(1L);
        assertThat(response.sellerNickname()).isEqualTo("seller");
        assertThat(response.primaryImageUrl()).isEqualTo("https://cdn.test/products/10-main.jpg");
        assertThat(response.thumbnailUrl()).isEqualTo("https://cdn.test/products/10-main.jpg");
        assertThat(response.imageUrls()).containsExactly(
            "https://cdn.test/products/10-main.jpg",
            "https://cdn.test/products/10-detail.jpg"
        );
    }

    @Test
    void getProductsIncludesViewerLikedAndCardMetadata() {
        ProductService service = newService();
        User seller = User.signup("seller@test.com", "password", "seller", "01011112222");
        assignId(seller, 1L);
        Region region = Region.create("Seoul Gangnam", "1168010100", "Seoul", "Gangnam", "Samseong");
        assignId(region, 5L);
        Product product = Product.create(
            seller,
            region,
            "Bike",
            "Good bike",
            BigDecimal.valueOf(50000),
            "SPORTS"
        );
        assignId(product, 10L);
        product.increaseLikeCount();
        ProductImage image = ProductImage.create(product, "https://cdn.test/products/10-main.jpg", 0);
        assignId(image, 100L);
        when(productRepository.findAllByRegionIdInAndDeletedAtIsNullAndStatusNotAndApprovalStatus(
            List.of(5L), ProductStatus.HIDDEN, ProductApprovalStatus.APPROVED, PageRequest.of(0, 20)
        )).thenReturn(new PageImpl<>(List.of(product)));
        when(productLikeRepository.findLikedProductIdsByUserIdAndProductIdIn(2L, List.of(10L))).thenReturn(List.of(10L));
        when(productImageRepository.findAllByProductIdInOrderByProductIdAscSortOrderAsc(List.of(10L)))
            .thenReturn(List.of(image));

        List<ProductResponse> responses = service.getProducts(2L, 5L, PageRequest.of(0, 20));

        assertThat(responses).hasSize(1);
        ProductResponse response = responses.get(0);
        assertThat(response.liked()).isTrue();
        assertThat(response.likeCount()).isEqualTo(1);
        assertThat(response.regionFullName()).isEqualTo("Seoul Gangnam");
        assertThat(response.sellerNickname()).isEqualTo("seller");
        assertThat(response.primaryImageUrl()).isEqualTo("https://cdn.test/products/10-main.jpg");
    }
}
