// ProductLikeService 단위 테스트
package com.team7.agora.domain.product.service;
import static com.team7.agora.support.TestEntityIds.assignId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.team7.agora.domain.product.dto.response.ProductLikeResponse;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.entity.ProductImage;
import com.team7.agora.domain.product.entity.ProductLike;
import com.team7.agora.domain.product.repository.ProductImageRepository;
import com.team7.agora.domain.product.repository.ProductLikeRepository;
import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserStatus;
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
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class ProductLikeServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductLikeRepository productLikeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    private ProductLikeService service;
    private User user;
    private Product product;

    private void setUpFixtures() {
        service = new ProductLikeService(productRepository, productLikeRepository, userRepository, productImageRepository);
        User seller = User.signup("seller@test.com", "password", "판매자", "01011112222");
        assignId(seller, 1L);
        user = User.signup("user@test.com", "password", "구매자", "01033334444");
        assignId(user, 2L);
        Region region = Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동");
        product = Product.create(seller, region, "자전거", "상태 좋아요", BigDecimal.valueOf(50000), "스포츠");
        assignId(product, 10L);
    }

    @Test
    void likeIncreasesLikeCount() {
        setUpFixtures();
        when(productRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(product));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(2L, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(productLikeRepository.existsByProductAndUser(product, user)).thenReturn(false);
        when(productRepository.findLikeCountById(10L)).thenReturn(1);

        ProductLikeResponse response = service.like(2L, 10L);

        assertThat(response.liked()).isTrue();
        assertThat(response.likeCount()).isEqualTo(1);
        verify(productRepository).increaseLikeCount(10L);
    }

    @Test
    void likeRejectsDuplicateLike() {
        setUpFixtures();
        when(productRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(product));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(2L, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(productLikeRepository.existsByProductAndUser(product, user)).thenReturn(true);

        assertThatThrownBy(() -> service.like(2L, 10L))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void likeMapsDuplicateConstraintViolationToBusinessException() {
        setUpFixtures();
        when(productRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(product));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(2L, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(productLikeRepository.existsByProductAndUser(product, user)).thenReturn(false);
        when(productLikeRepository.save(org.mockito.ArgumentMatchers.any(ProductLike.class)))
            .thenThrow(new DataIntegrityViolationException("duplicate product like"));

        assertThatThrownBy(() -> service.like(2L, 10L))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void unlikeDecreasesLikeCount() {
        setUpFixtures();
        product.increaseLikeCount();
        ProductLike productLike = ProductLike.create(product, user);
        assignId(productLike, 100L);

        when(productRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(product));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(2L, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(productLikeRepository.findByProductAndUser(product, user)).thenReturn(Optional.of(productLike));
        when(productRepository.findLikeCountById(10L)).thenReturn(0);

        ProductLikeResponse response = service.unlike(2L, 10L);

        assertThat(response.liked()).isFalse();
        assertThat(response.likeCount()).isEqualTo(0);
        verify(productRepository).decreaseLikeCount(10L);
    }

    @Test
    void unlikeRejectsWhenNotLiked() {
        setUpFixtures();
        when(productRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(product));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(2L, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(productLikeRepository.findByProductAndUser(product, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.unlike(2L, 10L))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void getMyLikedProductsReturnsLikedProducts() {
        setUpFixtures();
        ProductLike productLike = ProductLike.create(product, user);
        assignId(productLike, 100L);
        ProductImage image = ProductImage.create(product, "https://cdn.test/products/10-main.jpg", 0);
        assignId(image, 200L);

        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(2L, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(productLikeRepository.findAllByUser(user)).thenReturn(List.of(productLike));
        when(productImageRepository.findAllByProductIdInOrderByProductIdAscSortOrderAsc(List.of(10L)))
            .thenReturn(List.of(image));

        var responses = service.getMyLikedProducts(2L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).productId()).isEqualTo(10L);
        assertThat(responses.get(0).liked()).isTrue();
        assertThat(responses.get(0).sellerId()).isEqualTo(1L);
        assertThat(responses.get(0).regionName()).isEqualTo(product.getRegion().getName());
        assertThat(responses.get(0).primaryImageUrl()).isEqualTo("https://cdn.test/products/10-main.jpg");
    }

    @Test
    void likeRejectsNonActiveUser() {
        setUpFixtures();
        when(productRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(product));
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(2L, UserStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.like(2L, 10L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.NOT_FOUND);
    }
}
