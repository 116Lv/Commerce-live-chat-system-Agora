package com.team7.agora.domain.admin.service;

import com.team7.agora.domain.admin.dto.response.AdminApprovalRequestResponse;
import com.team7.agora.domain.admin.dto.response.AdminProductResponse;
import com.team7.agora.domain.admin.entity.Admin;
import com.team7.agora.domain.admin.entity.AdminApprovalRequest;
import com.team7.agora.domain.admin.enums.AdminPermission;
import com.team7.agora.domain.admin.repository.AdminApprovalRequestRepository;
import com.team7.agora.domain.admin.repository.AdminRepository;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.entity.ProductImage;
import com.team7.agora.domain.product.enums.ProductApprovalStatus;
import com.team7.agora.domain.product.enums.ProductStatus;
import com.team7.agora.domain.product.repository.ProductImageRepository;
import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.report.repository.ReportRepository;
import com.team7.agora.domain.search.service.ProductSearchService;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminProductService {

    private final ProductRepository productRepository;
    private final ReportRepository reportRepository;
    private final ProductSearchService productSearchService;
    private final ProductImageRepository productImageRepository;
    private final AdminRepository adminRepository;
    private final AdminApprovalRequestRepository approvalRequestRepository;

    public AdminProductService(
            ProductRepository productRepository,
            ReportRepository reportRepository,
            ProductSearchService productSearchService,
            ProductImageRepository productImageRepository,
            AdminRepository adminRepository,
            AdminApprovalRequestRepository approvalRequestRepository
    ) {
        this.productRepository = productRepository;
        this.reportRepository = reportRepository;
        this.productSearchService = productSearchService;
        this.productImageRepository = productImageRepository;
        this.adminRepository = adminRepository;
        this.approvalRequestRepository = approvalRequestRepository;
    }

    public Page<AdminProductResponse> getProducts(AdminPrincipal admin, boolean reportedOnly, Pageable pageable) {
        return getProducts(admin, reportedOnly, null, pageable);
    }

    public Page<AdminProductResponse> getProducts(
            AdminPrincipal admin,
            boolean reportedOnly,
            String approvalStatus,
            Pageable pageable
    ) {
        return getProducts(admin, reportedOnly, null, null, null, approvalStatus, pageable);
    }

    public Page<AdminProductResponse> getProducts(
            AdminPrincipal admin,
            boolean reportedOnly,
            String keyword,
            String sellerKeyword,
            String status,
            String approvalStatus,
            Pageable pageable
    ) {
        validateProductAdmin(admin);
        String normalizedKeyword = normalizeFilter(keyword);
        String normalizedSellerKeyword = normalizeFilter(sellerKeyword);
        Long sellerIdKeyword = parseSellerIdKeyword(normalizedKeyword);
        ProductStatus parsedStatus = parseStatus(status);
        ProductApprovalStatus parsedApprovalStatus = parseApprovalStatus(approvalStatus);
        Page<Product> products;
        boolean hasDomainFilters = normalizedKeyword != null || normalizedSellerKeyword != null || parsedStatus != null;

        if (reportedOnly && hasDomainFilters) {
            products = reportRepository.findDistinctReportedProductsByFilters(
                    normalizedKeyword,
                    normalizedSellerKeyword,
                    sellerIdKeyword,
                    parsedStatus,
                    parsedApprovalStatus,
                    pageable
            );
        } else if (reportedOnly) {
            products = parsedApprovalStatus == null
                    ? reportRepository.findDistinctReportedProducts(pageable)
                    : reportRepository.findDistinctReportedProductsByApprovalStatus(parsedApprovalStatus, pageable);
        } else if (parsedApprovalStatus != null) {
            products = productRepository.findAdminProducts(
                    normalizedKeyword,
                    normalizedSellerKeyword,
                    sellerIdKeyword,
                    parsedStatus,
                    parsedApprovalStatus,
                    pageable
            );
        } else if (hasDomainFilters) {
            products = productRepository.findAdminProducts(
                    normalizedKeyword,
                    normalizedSellerKeyword,
                    sellerIdKeyword,
                    parsedStatus,
                    null,
                    pageable
            );
        } else {
            products = productRepository.findAll(pageable);
        }

        Map<Long, List<String>> imageUrls = findImageUrls(products.getContent());
        return products.map(product -> AdminProductResponse.from(product, imageUrls.getOrDefault(product.getId(), List.of())));
    }

    @Transactional
    public AdminProductResponse hideProduct(AdminPrincipal admin, Long productId) {
        validateProductAdmin(admin);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Product not found."));
        product.hide();
        productSearchService.evictSearchCache();
        return AdminProductResponse.from(product);
    }

    @Transactional
    public AdminApprovalRequestResponse requestHideProduct(AdminPrincipal admin, Long productId, String reason) {
        validateProductAdmin(admin);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Product not found."));
        Admin requester = getCurrentAdmin(admin);
        String pendingRequestKey = AdminApprovalRequest.buildProductHidePendingRequestKey(productId);
        if (approvalRequestRepository.existsByPendingRequestKey(pendingRequestKey)) {
            throw new BusinessException(ErrorCode.CONFLICT, "A pending product hide approval request already exists.");
        }

        AdminApprovalRequest request = AdminApprovalRequest.createProductHide(requester, productId, product.getTitle(), reason);
        return AdminApprovalRequestResponse.from(approvalRequestRepository.save(request));
    }

    @Transactional
    public AdminProductResponse approveProduct(AdminPrincipal admin, Long productId) {
        validateProductAdmin(admin);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Product not found."));
        product.approve();
        productSearchService.evictSearchCache();
        return AdminProductResponse.from(product);
    }

    private String normalizeFilter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private ProductStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        try {
            return ProductStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Invalid product status.");
        }
    }

    private ProductApprovalStatus parseApprovalStatus(String approvalStatus) {
        if (approvalStatus == null || approvalStatus.isBlank()) {
            return null;
        }

        try {
            return ProductApprovalStatus.valueOf(approvalStatus.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Invalid product approval status.");
        }
    }

    private Long parseSellerIdKeyword(String keyword) {
        if (keyword == null || !keyword.matches("\\d+")) {
            return null;
        }

        try {
            return Long.valueOf(keyword);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Map<Long, List<String>> findImageUrls(List<Product> products) {
        List<Long> productIds = products.stream()
                .map(Product::getId)
                .toList();
        Map<Long, List<String>> imageUrls = new LinkedHashMap<>();
        if (productIds.isEmpty()) {
            return imageUrls;
        }

        List<ProductImage> images = productImageRepository.findAllByProductIdInOrderByProductIdAscSortOrderAsc(productIds);
        if (images == null) {
            return imageUrls;
        }

        for (ProductImage image : images) {
            imageUrls.computeIfAbsent(image.getProduct().getId(), ignored -> new java.util.ArrayList<>())
                    .add(image.getImageUrl());
        }
        return imageUrls;
    }

    private Admin getCurrentAdmin(AdminPrincipal admin) {
        if (admin == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return adminRepository.findById(admin.getAdminId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Admin account not found."));
    }

    private void validateProductAdmin(AdminPrincipal admin) {
        if (!AdminRoleSupport.hasPermission(admin, AdminPermission.PRODUCT_MANAGE)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Only product admins can manage products.");
        }
    }
}
