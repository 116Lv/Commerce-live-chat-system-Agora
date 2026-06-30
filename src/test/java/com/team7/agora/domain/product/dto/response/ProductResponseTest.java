package com.team7.agora.domain.product.dto.response;

import static com.team7.agora.support.TestEntityIds.assignId;
import static org.assertj.core.api.Assertions.assertThat;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.enums.ProductStatus;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.user.entity.User;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProductResponseTest {

    @Test
    void fromIncludesProductMetadataWithDefaultLikedFalse() {
        User seller = User.signup("seller@test.com", "password", "seller", "01011112222");
        assignId(seller, 1L);
        Region region = Region.create("Seoul Gangnam", "1168010100", "Seoul", "Gangnam", "Samseong");
        assignId(region, 5L);
        Product product = Product.create(seller, region, "Bike", "Good bike", BigDecimal.valueOf(50000), "SPORTS");
        assignId(product, 10L);
        product.increaseLikeCount();

        ProductResponse response = ProductResponse.from(product, "https://cdn.test/products/10-main.jpg");

        assertThat(response.productId()).isEqualTo(10L);
        assertThat(response.title()).isEqualTo("Bike");
        assertThat(response.likeCount()).isEqualTo(1);
        assertThat(response.liked()).isFalse();
        assertThat(response.regionId()).isEqualTo(5L);
        assertThat(response.regionName()).isEqualTo("Seoul Gangnam");
        assertThat(response.sellerId()).isEqualTo(1L);
        assertThat(response.sellerNickname()).isEqualTo("seller");
        assertThat(response.primaryImageUrl()).isEqualTo("https://cdn.test/products/10-main.jpg");
        assertThat(response.thumbnailUrl()).isEqualTo("https://cdn.test/products/10-main.jpg");
        assertThat(response.statusLabel()).isEqualTo("판매중");
        assertThat(response.categoryLabel()).isEqualTo("SPORTS");
    }

    @Test
    void fromCanExposeDetailImageUrlsSeparatelyFromCardThumbnail() {
        User seller = User.signup("seller@test.com", "password", "seller", "01011112222");
        assignId(seller, 1L);
        Region region = Region.create("Seoul Gangnam", "1168010100", "Seoul", "Gangnam", "Samseong");
        assignId(region, 5L);
        Product product = Product.create(seller, region, "Bike", "Good bike", BigDecimal.valueOf(50000), "SPORTS");
        assignId(product, 10L);

        ProductResponse response = ProductResponse.from(
            product,
            false,
            "https://cdn.test/products/10-main.jpg",
            List.of("https://cdn.test/products/10-main.jpg", "https://cdn.test/products/10-detail.jpg")
        );

        assertThat(response.primaryImageUrl()).isEqualTo("https://cdn.test/products/10-main.jpg");
        assertThat(response.thumbnailUrl()).isEqualTo("https://cdn.test/products/10-main.jpg");
        assertThat(response.imageUrls()).containsExactly(
            "https://cdn.test/products/10-main.jpg",
            "https://cdn.test/products/10-detail.jpg"
        );
    }

    @Test
    void legacyConstructorDefaultsAdditiveFields() {
        ProductResponse response = new ProductResponse(
            10L,
            "Bike",
            "Good bike",
            BigDecimal.valueOf(50000),
            "SPORTS",
            ProductStatus.SELLING
        );

        assertThat(response.likeCount()).isZero();
        assertThat(response.liked()).isFalse();
        assertThat(response.regionId()).isNull();
        assertThat(response.primaryImageUrl()).isNull();
    }
}
