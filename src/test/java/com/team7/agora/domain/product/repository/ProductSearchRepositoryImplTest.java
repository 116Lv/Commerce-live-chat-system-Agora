package com.team7.agora.domain.product.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.entity.ProductImage;
import com.team7.agora.domain.product.enums.ProductStatus;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.region.repository.RegionRepository;
import com.team7.agora.domain.search.dto.ProductSearchCondition;
import com.team7.agora.domain.search.dto.ProductSearchResponse;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ProductSearchRepositoryImplTest {

    private static final String KEYWORD_CATEGORY = "QDSL_IT_KEYWORD";
    private static final String HIDDEN_CATEGORY = "QDSL_IT_HIDDEN";
    private static final String PAGING_CATEGORY = "QDSL_IT_PAGING";

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private UserRepository userRepository;

    private Region seoul;
    private Region busan;
    private User seller;

    @BeforeEach
    void setUp() {
        seoul = regionRepository.save(Region.create("테스트 서울 강남구 역삼동", "9999999901", "서울", "강남구", "역삼동"));
        busan = regionRepository.save(Region.create("테스트 부산 해운대구 우동", "9999999902", "부산", "해운대구", "우동"));
        seller = userRepository.save(User.signup("seller-search-it@test.com", "encoded", "판매자", "01099998888"));
    }

    @Test
    void search_matchesKeywordInTitleOrDescriptionCaseInsensitively() {
        saveApproved(Product.create(
            seller, seoul, "Galaxy Book 정품", "거의 새 제품입니다", BigDecimal.valueOf(800000), KEYWORD_CATEGORY
        ));
        saveApproved(Product.create(
            seller, seoul, "자전거 팝니다", "최근에 산 GALAXY 보호필름 증정", BigDecimal.valueOf(50000), KEYWORD_CATEGORY
        ));
        saveApproved(Product.create(
            seller, seoul, "그릭요거트 메이커", "상관없는 상품", BigDecimal.valueOf(10000), KEYWORD_CATEGORY
        ));

        Page<ProductSearchResponse> results = productRepository.search(
            new ProductSearchCondition("galaxy", null, KEYWORD_CATEGORY, PageRequest.of(0, 20))
        );

        assertThat(results).extracting(ProductSearchResponse::title)
            .containsExactlyInAnyOrder("Galaxy Book 정품", "자전거 팝니다");
    }

    @Test
    void search_filtersByRegion() {
        String category = "QDSL_IT_REGION";
        saveApproved(Product.create(seller, seoul, "서울 상품", "설명", BigDecimal.valueOf(10000), category));
        saveApproved(Product.create(seller, busan, "부산 상품", "설명", BigDecimal.valueOf(10000), category));

        Page<ProductSearchResponse> results = productRepository.search(
            new ProductSearchCondition(null, seoul.getId(), category, PageRequest.of(0, 20))
        );

        assertThat(results).hasSize(1);
        assertThat(results.getContent().get(0).title()).isEqualTo("서울 상품");
        assertThat(results.getContent().get(0).regionName()).isEqualTo(seoul.getName());
    }

    @Test
    void search_filtersByCategory() {
        String category = "QDSL_IT_CATEGORY";
        saveApproved(Product.create(seller, seoul, "운동화", "설명", BigDecimal.valueOf(30000), category));
        saveApproved(Product.create(seller, seoul, "냄비", "설명", BigDecimal.valueOf(15000), "QDSL_IT_OTHER"));

        Page<ProductSearchResponse> results = productRepository.search(
            new ProductSearchCondition(null, null, category, PageRequest.of(0, 20))
        );

        assertThat(results).hasSize(1);
        assertThat(results.getContent().get(0).title()).isEqualTo("운동화");
    }

    @Test
    void search_returnsCardMetadata() {
        String category = "QDSL_IT_METADATA";
        Product product = Product.create(seller, seoul, "Card metadata bike", "description", BigDecimal.valueOf(30000), category);
        product.increaseLikeCount();
        product.approve();
        Product saved = productRepository.save(product);
        productImageRepository.save(ProductImage.create(saved, "https://cdn.test/products/search-main.jpg", 0));

        Page<ProductSearchResponse> results = productRepository.search(
            new ProductSearchCondition("metadata", null, category, PageRequest.of(0, 20))
        );

        assertThat(results).hasSize(1);
        ProductSearchResponse response = results.getContent().get(0);
        assertThat(response.id()).isEqualTo(saved.getId());
        assertThat(response.likeCount()).isEqualTo(1);
        assertThat(response.liked()).isFalse();
        assertThat(response.regionId()).isEqualTo(seoul.getId());
        assertThat(response.regionName()).isEqualTo(seoul.getName());
        assertThat(response.sellerId()).isEqualTo(seller.getId());
        assertThat(response.sellerNickname()).isEqualTo(seller.getNickname());
        assertThat(response.primaryImageUrl()).isEqualTo("https://cdn.test/products/search-main.jpg");
        assertThat(response.thumbnailUrl()).isEqualTo("https://cdn.test/products/search-main.jpg");
        assertThat(response.status()).isEqualTo(ProductStatus.SELLING);
        assertThat(response.statusLabel()).isEqualTo("판매중");
        assertThat(response.category()).isEqualTo(category);
        assertThat(response.categoryLabel()).isEqualTo(category);
    }

    @Test
    void search_excludesDeletedAndHiddenProducts() {
        Product deleted = productRepository.save(Product.create(
            seller, seoul, "삭제된 상품", "설명", BigDecimal.valueOf(1000), HIDDEN_CATEGORY
        ));
        deleted.delete();
        Product hidden = productRepository.save(Product.create(
            seller, seoul, "숨김 상품", "설명", BigDecimal.valueOf(1000), HIDDEN_CATEGORY
        ));
        hidden.hide();
        saveApproved(Product.create(seller, seoul, "정상 상품", "설명", BigDecimal.valueOf(1000), HIDDEN_CATEGORY));

        Page<ProductSearchResponse> results = productRepository.search(
            new ProductSearchCondition(null, null, HIDDEN_CATEGORY, PageRequest.of(0, 20))
        );

        assertThat(results).extracting(ProductSearchResponse::title).containsExactly("정상 상품");
    }

    @Test
    void search_appliesPagingOrderedByIdDescending() {
        Product first = saveApproved(Product.create(
            seller, seoul, "상품1", "설명", BigDecimal.valueOf(1000), PAGING_CATEGORY
        ));
        Product second = saveApproved(Product.create(
            seller, seoul, "상품2", "설명", BigDecimal.valueOf(1000), PAGING_CATEGORY
        ));
        saveApproved(Product.create(seller, seoul, "상품3", "설명", BigDecimal.valueOf(1000), PAGING_CATEGORY));

        Page<ProductSearchResponse> firstPage = productRepository.search(
            new ProductSearchCondition(null, null, PAGING_CATEGORY, PageRequest.of(0, 2))
        );
        Page<ProductSearchResponse> secondPage = productRepository.search(
            new ProductSearchCondition(null, null, PAGING_CATEGORY, PageRequest.of(1, 2))
        );

        assertThat(firstPage).hasSize(2);
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.getContent().get(0).id()).isEqualTo(first.getId());
        assertThat(firstPage.getContent().get(1).id()).isEqualTo(second.getId());
    }

    private Product saveApproved(Product product) {
        product.approve();
        return productRepository.save(product);
    }
}
