package com.team7.agora.domain.product.service;
import static com.team7.agora.support.TestEntityIds.assignId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.product.dto.request.ProductCreateRequest;
import com.team7.agora.domain.product.dto.request.ProductUpdateRequest;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.enums.ProductStatus;
import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.region.repository.RegionRepository;
import com.team7.agora.domain.region.repository.UserRegionRepository;
import com.team7.agora.domain.search.service.ProductSearchService;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

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
    void create_savesProductWithSellerAndRegion() {
        ProductService productService = new ProductService(productRepository, userRepository, regionRepository, userRegionRepository, productSearchService);
        User seller = User.signup("seller@test.com", "encoded", "판매자", "01011112222");
        Region region = Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동");
        ProductCreateRequest request = new ProductCreateRequest(
            "자전거",
            "상태 좋은 중고 자전거입니다.",
            BigDecimal.valueOf(73000),
            "SPORTS",
            1L
        );
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(1L, UserStatus.ACTIVE)).thenReturn(Optional.of(seller));
        when(regionRepository.findById(1L)).thenReturn(Optional.of(region));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        productService.create(1L, request);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product product = captor.getValue();
        assertThat(product.getTitle()).isEqualTo("자전거");
        assertThat(product.getPrice()).isEqualByComparingTo("73000");
        assertThat(product.getStatus()).isEqualTo(ProductStatus.SELLING);
    }

    @Test
    void create_evictsSearchCache() {
        ProductService productService = new ProductService(productRepository, userRepository, regionRepository, userRegionRepository, productSearchService);
        User seller = User.signup("seller@test.com", "encoded", "판매자", "01011112222");
        Region region = Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동");
        ProductCreateRequest request = new ProductCreateRequest(
            "자전거",
            "상태 좋은 중고 자전거입니다.",
            BigDecimal.valueOf(73000),
            "SPORTS",
            1L
        );
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(1L, UserStatus.ACTIVE)).thenReturn(Optional.of(seller));
        when(regionRepository.findById(1L)).thenReturn(Optional.of(region));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        productService.create(1L, request);

        verify(productSearchService).evictSearchCache();
    }

    @Test
    void create_rejectsNonActiveSeller() {
        ProductService productService = new ProductService(productRepository, userRepository, regionRepository, userRegionRepository, productSearchService);
        ProductCreateRequest request = new ProductCreateRequest(
            "자전거",
            "상태 좋은 중고 자전거입니다.",
            BigDecimal.valueOf(73000),
            "SPORTS",
            1L
        );
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(1L, UserStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(1L, request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void update_throwsForbiddenWhenRequesterIsNotSeller() {
        ProductService productService = new ProductService(productRepository, userRepository, regionRepository, userRegionRepository, productSearchService);
        User seller = User.signup("seller@test.com", "encoded", "판매자", "01011112222");
        assignId(seller, 1L);
        Product product = Product.create(
            seller,
            Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동"),
            "자전거",
            "상태 좋은 중고 자전거입니다.",
            BigDecimal.valueOf(73000),
            "SPORTS"
        );
        when(productRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(product));

        ProductUpdateRequest request = new ProductUpdateRequest("수정 제목", "수정 설명", BigDecimal.valueOf(70000), "SPORTS");

        assertThatThrownBy(() -> productService.update(2L, 1L, request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void update_evictsSearchCache() {
        ProductService productService = new ProductService(productRepository, userRepository, regionRepository, userRegionRepository, productSearchService);
        User seller = User.signup("seller@test.com", "encoded", "판매자", "01011112222");
        assignId(seller, 1L);
        Product product = Product.create(
            seller,
            Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동"),
            "자전거",
            "상태 좋은 중고 자전거입니다.",
            BigDecimal.valueOf(73000),
            "SPORTS"
        );
        when(productRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(1L, UserStatus.ACTIVE)).thenReturn(Optional.of(seller));
        ProductUpdateRequest request = new ProductUpdateRequest("수정 제목", "수정 설명", BigDecimal.valueOf(70000), "SPORTS");

        productService.update(1L, 1L, request);

        verify(productSearchService).evictSearchCache();
    }

    @Test
    void update_rejectsReservedProduct() {
        ProductService productService = new ProductService(productRepository, userRepository, regionRepository, userRegionRepository, productSearchService);
        User seller = User.signup("seller@test.com", "encoded", "seller", "01011112222");
        assignId(seller, 1L);
        Product product = Product.create(
            seller,
            Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동"),
            "자전거",
            "상태 좋은 중고 자전거입니다.",
            BigDecimal.valueOf(73000),
            "SPORTS"
        );
        product.markReserved();
        when(productRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(1L, UserStatus.ACTIVE)).thenReturn(Optional.of(seller));
        ProductUpdateRequest request = new ProductUpdateRequest("수정 제목", "수정 설명", BigDecimal.valueOf(70000), "SPORTS");

        assertThatThrownBy(() -> productService.update(1L, 1L, request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.CONFLICT);
    }

    @Test
    void update_rejectsNonActiveSeller() {
        ProductService productService = new ProductService(productRepository, userRepository, regionRepository, userRegionRepository, productSearchService);
        User seller = User.signup("seller@test.com", "encoded", "판매자", "01011112222");
        assignId(seller, 1L);
        Product product = Product.create(
            seller,
            Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동"),
            "자전거",
            "상태 좋은 중고 자전거입니다.",
            BigDecimal.valueOf(73000),
            "SPORTS"
        );
        when(productRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(1L, UserStatus.ACTIVE)).thenReturn(Optional.empty());
        ProductUpdateRequest request = new ProductUpdateRequest("수정 제목", "수정 설명", BigDecimal.valueOf(70000), "SPORTS");

        assertThatThrownBy(() -> productService.update(1L, 1L, request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void delete_marksProductDeletedWhenRequesterIsSeller() {
        ProductService productService = new ProductService(productRepository, userRepository, regionRepository, userRegionRepository, productSearchService);
        User seller = User.signup("seller@test.com", "encoded", "판매자", "01011112222");
        assignId(seller, 1L);
        Product product = Product.create(
            seller,
            Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동"),
            "자전거",
            "상태 좋은 중고 자전거입니다.",
            BigDecimal.valueOf(73000),
            "SPORTS"
        );
        when(productRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(1L, UserStatus.ACTIVE)).thenReturn(Optional.of(seller));

        productService.delete(1L, 1L);

        assertThat(product.getDeletedAt()).isNotNull();
    }

    @Test
    void delete_rejectsWhenRequesterIsNotSeller() {
        ProductService productService = new ProductService(productRepository, userRepository, regionRepository, userRegionRepository, productSearchService);
        User seller = User.signup("seller@test.com", "encoded", "판매자", "01011112222");
        assignId(seller, 1L);
        Product product = Product.create(
            seller,
            Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동"),
            "자전거",
            "상태 좋은 중고 자전거입니다.",
            BigDecimal.valueOf(73000),
            "SPORTS"
        );
        when(productRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.delete(2L, 1L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void delete_evictsSearchCache() {
        ProductService productService = new ProductService(productRepository, userRepository, regionRepository, userRegionRepository, productSearchService);
        User seller = User.signup("seller@test.com", "encoded", "판매자", "01011112222");
        assignId(seller, 1L);
        Product product = Product.create(
            seller,
            Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동"),
            "자전거",
            "상태 좋은 중고 자전거입니다.",
            BigDecimal.valueOf(73000),
            "SPORTS"
        );
        when(productRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(1L, UserStatus.ACTIVE)).thenReturn(Optional.of(seller));

        productService.delete(1L, 1L);

        verify(productSearchService).evictSearchCache();
    }

    @Test
    void delete_rejectsNonActiveSeller() {
        ProductService productService = new ProductService(productRepository, userRepository, regionRepository, userRegionRepository, productSearchService);
        User seller = User.signup("seller@test.com", "encoded", "판매자", "01011112222");
        assignId(seller, 1L);
        Product product = Product.create(
            seller,
            Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동"),
            "자전거",
            "상태 좋은 중고 자전거입니다.",
            BigDecimal.valueOf(73000),
            "SPORTS"
        );
        when(productRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(1L, UserStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(1L, 1L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.NOT_FOUND);
    }
}
