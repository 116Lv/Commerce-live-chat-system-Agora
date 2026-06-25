package com.team7.agora.domain.admin.service;

import static com.team7.agora.support.TestEntityIds.assignId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.admin.dto.response.AdminProductResponse;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.enums.ProductStatus;
import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.report.entity.Report;
import com.team7.agora.domain.report.repository.ReportRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.exception.BusinessException;
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
class AdminProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ReportRepository reportRepository;

    @Test
    void getProducts_returnsProductList() {
        AdminProductService service = new AdminProductService(productRepository, reportRepository);
        Product product = product(1L);
        when(productRepository.findAll(PageRequest.of(0, 20))).thenReturn(new PageImpl<>(List.of(product)));

        List<AdminProductResponse> responses = service.getProducts(principal(UserRole.PRODUCT_ADMIN), false, PageRequest.of(0, 20));

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).id()).isEqualTo(1L);
        assertThat(responses.get(0).title()).isEqualTo("중고 자전거");
    }

    @Test
    void getProducts_returnsReportedProductsOnly() {
        AdminProductService service = new AdminProductService(productRepository, reportRepository);
        Product product = product(1L);
        User reporter = user(2L, "reporter@test.com", "신고자");
        Report report = Report.product(reporter, product.getSeller(), product, "허위 매물입니다.");
        when(reportRepository.findAllByProductIsNotNull()).thenReturn(List.of(report, report));
        when(productRepository.findAllById(List.of(1L))).thenReturn(List.of(product));

        List<AdminProductResponse> responses = service.getProducts(principal(UserRole.ROOT_ADMIN), true, PageRequest.of(0, 20));

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).id()).isEqualTo(1L);
    }

    @Test
    void getProducts_rejectsNonProductAdmin() {
        AdminProductService service = new AdminProductService(productRepository, reportRepository);

        assertThatThrownBy(() -> service.getProducts(principal(UserRole.USER_ADMIN), false, PageRequest.of(0, 20)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void hideProduct_hidesProduct() {
        AdminProductService service = new AdminProductService(productRepository, reportRepository);
        Product product = product(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        AdminProductResponse response = service.hideProduct(principal(UserRole.PRODUCT_ADMIN), 1L);

        assertThat(response.status()).isEqualTo("HIDDEN");
        assertThat(product.getStatus()).isEqualTo(ProductStatus.HIDDEN);
    }

    @Test
    void hideProduct_rejectsNonProductAdmin() {
        AdminProductService service = new AdminProductService(productRepository, reportRepository);

        assertThatThrownBy(() -> service.hideProduct(principal(UserRole.USER_ADMIN), 1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void hideProduct_throwsNotFoundWhenProductMissing() {
        AdminProductService service = new AdminProductService(productRepository, reportRepository);
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.hideProduct(principal(UserRole.PRODUCT_ADMIN), 1L))
                .isInstanceOf(BusinessException.class);
    }

    private Product product(Long id) {
        User seller = user(10L, "seller@test.com", "판매자");
        Region region = Region.create("서울 강남구 역삼동", "1168010100", "서울", "강남구", "역삼동");
        assignId(region, 1L);
        Product product = Product.create(seller, region, "중고 자전거", "상태 좋음", BigDecimal.valueOf(100000), "SPORTS");
        assignId(product, id);
        return product;
    }

    private User user(Long id, String email, String nickname) {
        User user = User.signup(email, "encoded", nickname, "01011112222");
        assignId(user, id);
        return user;
    }

    private CustomUserDetails principal(UserRole role) {
        return new CustomUserDetails(99L, "admin@test.com", "encoded", role, UserStatus.ACTIVE, "관리자");
    }
}
