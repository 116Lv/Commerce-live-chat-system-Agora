package com.team7.agora.domain.admin.service;

import com.team7.agora.domain.admin.dto.response.AdminProductResponse;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.report.repository.ReportRepository;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자 상품 관련 비즈니스 유스케이스를 처리하는 서비스이다.
 */
@Service
@Transactional(readOnly = true)
public class AdminProductService {

    private final ProductRepository productRepository;
    private final ReportRepository reportRepository;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param productRepository 데이터를 조회하고 저장하는 리포지토리
     * @param reportRepository 데이터를 조회하고 저장하는 리포지토리
     */
    public AdminProductService(ProductRepository productRepository, ReportRepository reportRepository) {
        this.productRepository = productRepository;
        this.reportRepository = reportRepository;
    }

    /**
     * 'getProducts' 메서드는 필요한 데이터를 조회해 호출한 쪽에 반환한다.
     * @param admin 인증된 관리자 정보
     * @param reportedOnly 신고 상품만 조회할지 여부
     * @param pageable 페이지 요청 정보
     * @return 클라이언트에 반환할 API 응답
     */
    public Page<AdminProductResponse> getProducts(AdminPrincipal admin, boolean reportedOnly, Pageable pageable) {
        validateProductAdmin(admin);
        Page<Product> products = reportedOnly
                ? reportRepository.findDistinctReportedProducts(pageable)
                : productRepository.findAll(pageable);
        return products.map(AdminProductResponse::from);
    }

    /**
     * 관리자가 신고나 정책 위반 상품을 숨김 상태로 변경한다.
     * @param admin 인증된 관리자 정보
     * @param productId 상품 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @Transactional
    public AdminProductResponse hideProduct(AdminPrincipal admin, Long productId) {
        validateProductAdmin(admin);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));
        product.hide();
        return AdminProductResponse.from(product);
    }

    private void validateProductAdmin(AdminPrincipal admin) {
        if (admin == null || !AdminRoleSupport.isProductAdminRole(admin.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "상품 관리는 관리자만 수행할 수 있습니다.");
        }
    }
}
