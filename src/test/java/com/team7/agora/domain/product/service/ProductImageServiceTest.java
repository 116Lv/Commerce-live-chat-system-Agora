// ProductImageService 단위 테스트
package com.team7.agora.domain.product.service;
import static com.team7.agora.support.TestEntityIds.assignId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.product.dto.response.ProductImageResponse;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.entity.ProductImage;
import com.team7.agora.domain.product.repository.ProductImageRepository;
import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.storage.ImageStorageClient;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ProductImageServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ImageStorageClient imageStorageClient;

    private ProductImageService service;
    private User seller;
    private User stranger;
    private Product product;

    private void setUpFixtures() {
        service = new ProductImageService(productRepository, productImageRepository, imageStorageClient);
        seller = User.signup("seller@test.com", "password", "판매자", "01011112222");
        assignId(seller, 1L);
        stranger = User.signup("stranger@test.com", "password", "제3자", "01055556666");
        assignId(stranger, 2L);
        Region region = Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동");
        product = Product.create(seller, region, "자전거", "상태 좋아요", BigDecimal.valueOf(50000), "스포츠");
        assignId(product, 10L);
    }

    @Test
    void uploadSavesImageForSeller() {
        setUpFixtures();
        MultipartFile file = new MockMultipartFile("image", "photo.jpg", "image/jpeg", "data".getBytes());

        when(productRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(product));
        when(imageStorageClient.store("products", file)).thenReturn("/uploads/products/abc.jpg");
        when(productImageRepository.countByProduct(product)).thenReturn(0);
        when(productImageRepository.save(any(ProductImage.class))).thenAnswer(invocation -> {
            ProductImage image = invocation.getArgument(0);
            assignId(image, 100L);
            return image;
        });

        ProductImageResponse response = service.upload(1L, 10L, file);

        assertThat(response.imageId()).isEqualTo(100L);
        assertThat(response.imageUrl()).isEqualTo("/uploads/products/abc.jpg");
        assertThat(response.sortOrder()).isEqualTo(0);
    }

    @Test
    void uploadRejectsNonSeller() {
        setUpFixtures();
        MultipartFile file = new MockMultipartFile("image", "photo.jpg", "image/jpeg", "data".getBytes());

        when(productRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> service.upload(2L, 10L, file))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void uploadRejectsWhenProductNotFound() {
        setUpFixtures();
        MultipartFile file = new MockMultipartFile("image", "photo.jpg", "image/jpeg", "data".getBytes());

        when(productRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.upload(1L, 10L, file))
            .isInstanceOf(BusinessException.class);
    }
}
