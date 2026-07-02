package com.team7.agora.domain.admin.service;

import static com.team7.agora.support.TestEntityIds.assignId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.admin.entity.Admin;
import com.team7.agora.domain.admin.entity.AdminApprovalRequest;
import com.team7.agora.domain.admin.enums.AdminApprovalOperation;
import com.team7.agora.domain.admin.dto.response.AdminProductResponse;
import com.team7.agora.domain.admin.enums.AdminRole;
import com.team7.agora.domain.admin.enums.AdminStatus;
import com.team7.agora.domain.admin.repository.AdminApprovalRequestRepository;
import com.team7.agora.domain.admin.repository.AdminRepository;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.entity.ProductImage;
import com.team7.agora.domain.product.enums.ProductApprovalStatus;
import com.team7.agora.domain.product.enums.ProductStatus;
import com.team7.agora.domain.product.repository.ProductImageRepository;
import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.report.repository.ReportRepository;
import com.team7.agora.domain.search.service.ProductSearchService;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.exception.BusinessException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class AdminProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ProductSearchService productSearchService;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private AdminApprovalRequestRepository approvalRequestRepository;

    @Test
    void getProducts_returnsProductList() {
        AdminProductService service = createService();
        Product product = product(1L);
        when(productRepository.findAll(PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(product), PageRequest.of(0, 20), 42));

        Page<AdminProductResponse> responses = service.getProducts(principal(AdminRole.PRODUCT_ADMIN), false, PageRequest.of(0, 20));

        assertThat(responses.getContent()).hasSize(1);
        assertThat(responses.getContent().get(0).id()).isEqualTo(1L);
        assertThat(responses.getContent().get(0).title()).isEqualTo("Bike");
        assertThat(responses.getContent().get(0).description()).isEqualTo("Good condition");
        assertThat(responses.getContent().get(0).createdAt()).isNotNull();
        assertThat(responses.getTotalElements()).isEqualTo(42);
        assertThat(responses.getTotalPages()).isEqualTo(3);
    }

    @Test
    void getProducts_filtersByApprovalStatusAndIncludesSellerNickname() {
        AdminProductService service = createService();
        Product product = product(1L);
        product.hide();
        when(productRepository.findAdminProducts(
                null,
                null,
                null,
                null,
                ProductApprovalStatus.REJECTED,
                PageRequest.of(0, 20)
        ))
                .thenReturn(new PageImpl<>(List.of(product), PageRequest.of(0, 20), 1));

        Page<AdminProductResponse> responses = service.getProducts(
                principal(AdminRole.PRODUCT_ADMIN),
                false,
                "REJECTED",
                PageRequest.of(0, 20)
        );

        AdminProductResponse response = responses.getContent().get(0);
        assertThat(response.approvalStatus()).isEqualTo("REJECTED");
        assertThat(response.sellerNickname()).isEqualTo("seller");
        assertThat(response.statusLabel()).isNotBlank();
    }

    @Test
    void getProducts_filtersPendingApprovalStatus() {
        AdminProductService service = createService();
        Product product = product(1L);
        when(productRepository.findAdminProducts(
                null,
                null,
                null,
                null,
                ProductApprovalStatus.PENDING,
                PageRequest.of(0, 20)
        ))
                .thenReturn(new PageImpl<>(List.of(product), PageRequest.of(0, 20), 1));

        Page<AdminProductResponse> responses = service.getProducts(
                principal(AdminRole.PRODUCT_ADMIN),
                false,
                "PENDING",
                PageRequest.of(0, 20)
        );

        assertThat(responses.getContent().get(0).approvalStatus()).isEqualTo("PENDING");
    }

    @Test
    void getProducts_filtersApprovedStatusAcrossVisibleProductStatuses() {
        AdminProductService service = createService();
        Product product = product(1L);
        product.approve();
        when(productRepository.findAdminProducts(
                null,
                null,
                null,
                null,
                ProductApprovalStatus.APPROVED,
                PageRequest.of(0, 20)
        ))
                .thenReturn(new PageImpl<>(List.of(product), PageRequest.of(0, 20), 1));

        Page<AdminProductResponse> responses = service.getProducts(
                principal(AdminRole.PRODUCT_ADMIN),
                false,
                "APPROVED",
                PageRequest.of(0, 20)
        );

        assertThat(responses.getContent().get(0).approvalStatus()).isEqualTo("APPROVED");
    }

    @Test
    void getProducts_returnsReportedProductsWithPageable() {
        AdminProductService service = createService();
        Product product = product(1L);
        PageRequest pageable = PageRequest.of(1, 1);
        when(reportRepository.findDistinctReportedProducts(pageable))
                .thenReturn(new PageImpl<>(List.of(product), pageable, 3));

        Page<AdminProductResponse> responses = service.getProducts(principal(AdminRole.ROOT_ADMIN), true, pageable);

        assertThat(responses.getContent()).hasSize(1);
        assertThat(responses.getContent().get(0).id()).isEqualTo(1L);
        assertThat(responses.getNumber()).isEqualTo(1);
        assertThat(responses.getSize()).isEqualTo(1);
        assertThat(responses.getTotalElements()).isEqualTo(3);
        verify(productRepository, never()).findAllById(List.of(1L));
    }

    @Test
    void getProducts_composesReportedOnlyAndApprovalStatusFilters() {
        AdminProductService service = createService();
        Product pendingProduct = product(1L);
        PageRequest pageable = PageRequest.of(0, 20);
        when(reportRepository.findDistinctReportedProductsByApprovalStatus(ProductApprovalStatus.PENDING, pageable))
                .thenReturn(new PageImpl<>(List.of(pendingProduct), pageable, 1));

        Page<AdminProductResponse> responses = service.getProducts(
                principal(AdminRole.PRODUCT_ADMIN),
                true,
                "PENDING",
                pageable
        );

        assertThat(responses.getContent()).hasSize(1);
        assertThat(responses.getContent().get(0).id()).isEqualTo(1L);
        assertThat(responses.getContent().get(0).approvalStatus()).isEqualTo("PENDING");
        verify(reportRepository).findDistinctReportedProductsByApprovalStatus(ProductApprovalStatus.PENDING, pageable);
        verify(reportRepository, never()).findDistinctReportedProducts(pageable);
    }

    @Test
    void getProducts_delegatesDomainSearchConditionsToProductRepository() {
        AdminProductService service = createService();
        Product product = product(1L);
        PageRequest pageable = PageRequest.of(0, 20);
        when(productRepository.findAdminProducts(
                "Bike",
                "seller",
                null,
                ProductStatus.SELLING,
                ProductApprovalStatus.APPROVED,
                pageable
        )).thenReturn(new PageImpl<>(List.of(product), pageable, 1));

        Page<AdminProductResponse> responses = service.getProducts(
                principal(AdminRole.PRODUCT_ADMIN),
                false,
                " Bike ",
                " seller ",
                "SELLING",
                "APPROVED",
                pageable
        );

        assertThat(responses.getContent()).hasSize(1);
        verify(productRepository).findAdminProducts(
                "Bike",
                "seller",
                null,
                ProductStatus.SELLING,
                ProductApprovalStatus.APPROVED,
                pageable
        );
    }

    @Test
    void getProducts_delegatesReportedDomainSearchConditionsToReportRepository() {
        AdminProductService service = createService();
        Product product = product(1L);
        PageRequest pageable = PageRequest.of(0, 20);
        when(reportRepository.findDistinctReportedProductsByFilters(
                "Bike",
                "seller",
                null,
                ProductStatus.SELLING,
                ProductApprovalStatus.PENDING,
                pageable
        )).thenReturn(new PageImpl<>(List.of(product), pageable, 1));

        Page<AdminProductResponse> responses = service.getProducts(
                principal(AdminRole.PRODUCT_ADMIN),
                true,
                "Bike",
                "seller",
                "SELLING",
                "PENDING",
                pageable
        );

        assertThat(responses.getContent()).hasSize(1);
        verify(reportRepository).findDistinctReportedProductsByFilters(
                "Bike",
                "seller",
                null,
                ProductStatus.SELLING,
                ProductApprovalStatus.PENDING,
                pageable
        );
    }

    @Test
    void requestHideProductCreatesPendingApprovalWithoutHidingProduct() {
        AdminProductService service = createService();
        Product product = product(1L);
        Admin requester = admin(99L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(adminRepository.findById(99L)).thenReturn(Optional.of(requester));
        when(approvalRequestRepository.existsByPendingRequestKey("PRODUCT_HIDE:1")).thenReturn(false);
        when(approvalRequestRepository.save(org.mockito.ArgumentMatchers.any(AdminApprovalRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.requestHideProduct(principal(AdminRole.PRODUCT_ADMIN), 1L, "신고 누적");

        assertThat(response.operation()).isEqualTo(AdminApprovalOperation.PRODUCT_HIDE.name());
        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.targetProductId()).isEqualTo(1L);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.SELLING);
        assertThat(product.getApprovalStatus()).isEqualTo(ProductApprovalStatus.PENDING);
        verify(productSearchService, never()).evictSearchCache();
    }

    @Test
    void requestHideProduct_translatesPendingRequestUniqueRaceToConflict() {
        AdminProductService service = createService();
        Product product = product(1L);
        Admin requester = admin(99L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(adminRepository.findById(99L)).thenReturn(Optional.of(requester));
        when(approvalRequestRepository.existsByPendingRequestKey("PRODUCT_HIDE:1")).thenReturn(false);
        when(approvalRequestRepository.save(org.mockito.ArgumentMatchers.any(AdminApprovalRequest.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate pending request"));

        assertThatThrownBy(() -> service.requestHideProduct(principal(AdminRole.PRODUCT_ADMIN), 1L, "신고 누적"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("pending product hide approval request");
    }

    @Test
    void requestHideProduct_rejectsAlreadyHiddenProduct() {
        AdminProductService service = createService();
        Product product = product(1L);
        product.hide();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> service.requestHideProduct(principal(AdminRole.PRODUCT_ADMIN), 1L, "신고 누적"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already hidden");
    }

    @Test
    void getProducts_rejectsNonProductAdmin() {
        AdminProductService service = createService();

        assertThatThrownBy(() -> service.getProducts(principal(AdminRole.USER_ADMIN), false, PageRequest.of(0, 20)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void hideProduct_hidesProduct() {
        AdminProductService service = createService();
        Product product = product(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productImageRepository.findAllByProductIdInOrderByProductIdAscSortOrderAsc(List.of(1L)))
                .thenReturn(List.of(ProductImage.create(product, "/uploads/products/bike.jpg", 0)));

        AdminProductResponse response = service.hideProduct(principal(AdminRole.PRODUCT_ADMIN), 1L);

        assertThat(response.status()).isEqualTo("HIDDEN");
        assertThat(response.approvalStatus()).isEqualTo("REJECTED");
        assertThat(response.imageUrls()).containsExactly("/uploads/products/bike.jpg");
        assertThat(product.getStatus()).isEqualTo(ProductStatus.HIDDEN);
        assertThat(product.getApprovalStatus()).isEqualTo(ProductApprovalStatus.REJECTED);
        verify(productSearchService).evictSearchCache();
    }

    @Test
    void approveProduct_restoresSellingStatusForProductAdmin() {
        AdminProductService service = createService();
        Product product = product(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productImageRepository.findAllByProductIdInOrderByProductIdAscSortOrderAsc(List.of(1L)))
                .thenReturn(List.of(ProductImage.create(product, "/uploads/products/bike.jpg", 0)));

        AdminProductResponse response = service.approveProduct(principal(AdminRole.PRODUCT_ADMIN), 1L);

        assertThat(response.status()).isEqualTo("SELLING");
        assertThat(response.approvalStatus()).isEqualTo("APPROVED");
        assertThat(response.imageUrls()).containsExactly("/uploads/products/bike.jpg");
        assertThat(product.getStatus()).isEqualTo(ProductStatus.SELLING);
        assertThat(product.getApprovalStatus()).isEqualTo(ProductApprovalStatus.APPROVED);
        verify(productSearchService).evictSearchCache();
    }

    @Test
    void hideProduct_rejectsNonProductAdmin() {
        AdminProductService service = createService();

        assertThatThrownBy(() -> service.hideProduct(principal(AdminRole.USER_ADMIN), 1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void hideProduct_throwsNotFoundWhenProductMissing() {
        AdminProductService service = createService();
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.hideProduct(principal(AdminRole.PRODUCT_ADMIN), 1L))
                .isInstanceOf(BusinessException.class);
    }

    private Product product(Long id) {
        User seller = user(10L, "seller@test.com", "seller");
        Region region = Region.create("Seoul Gangnam", "1168010100", "Seoul", "Gangnam", "Yeoksam");
        assignId(region, 1L);
        Product product = Product.create(seller, region, "Bike", "Good condition", BigDecimal.valueOf(100000), "SPORTS");
        assignId(product, id);
        return product;
    }

    private AdminProductService createService() {
        return new AdminProductService(
                productRepository,
                reportRepository,
                productSearchService,
                productImageRepository,
                adminRepository,
                approvalRequestRepository
        );
    }

    private User user(Long id, String email, String nickname) {
        User user = User.signup(email, "encoded", nickname, "01011112222");
        assignId(user, id);
        return user;
    }

    private AdminPrincipal principal(AdminRole role) {
        return new AdminPrincipal(99L, "admin@test.com", "encoded", role, AdminStatus.ACTIVE, "admin");
    }

    private Admin admin(Long id) {
        Admin admin = Admin.create("admin@test.com", "encoded", "admin", AdminRole.PRODUCT_ADMIN);
        assignId(admin, id);
        return admin;
    }
}
