package com.team7.agora.domain.admin.service;

import com.team7.agora.domain.admin.dto.response.AdminProductResponse;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.report.repository.ReportRepository;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminProductService {

    private final ProductRepository productRepository;
    private final ReportRepository reportRepository;

    public AdminProductService(ProductRepository productRepository, ReportRepository reportRepository) {
        this.productRepository = productRepository;
        this.reportRepository = reportRepository;
    }

    public List<AdminProductResponse> getProducts(CustomUserDetails admin, boolean reportedOnly, Pageable pageable) {
        validateProductAdmin(admin);
        if (reportedOnly) {
            List<Long> reportedProductIds = reportRepository.findAllByProductIsNotNull().stream()
                    .map(report -> report.getProduct().getId())
                    .distinct()
                    .toList();
            return productRepository.findAllById(reportedProductIds).stream()
                    .map(AdminProductResponse::from)
                    .toList();
        }

        return productRepository.findAll(pageable).stream()
                .map(AdminProductResponse::from)
                .toList();
    }

    @Transactional
    public AdminProductResponse hideProduct(CustomUserDetails admin, Long productId) {
        validateProductAdmin(admin);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));
        product.hide();
        return AdminProductResponse.from(product);
    }

    private void validateProductAdmin(CustomUserDetails admin) {
        if (admin == null || !AdminRoleSupport.isProductAdminRole(admin.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "상품 관리는 관리자만 수행할 수 있습니다.");
        }
    }
}
