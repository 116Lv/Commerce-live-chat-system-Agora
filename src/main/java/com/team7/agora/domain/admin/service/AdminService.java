package com.team7.agora.domain.admin.service;

import com.team7.agora.domain.admin.dto.response.AdminDashboardResponse;
import com.team7.agora.domain.admin.dto.response.AdminDashboardResponse.PendingReportResponse;
import com.team7.agora.domain.admin.dto.response.AdminMeResponse;
import com.team7.agora.domain.admin.entity.Admin;
import com.team7.agora.domain.admin.enums.AdminPermission;
import com.team7.agora.domain.admin.repository.AdminRepository;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.enums.ProductApprovalStatus;
import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.report.entity.Report;
import com.team7.agora.domain.report.enums.ReportStatus;
import com.team7.agora.domain.report.repository.ReportRepository;
import com.team7.agora.domain.trade.repository.TradeRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import com.team7.agora.global.time.AgoraClock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final ProductRepository productRepository;
    private final TradeRepository tradeRepository;

    public AdminService(
            AdminRepository adminRepository,
            UserRepository userRepository,
            ReportRepository reportRepository,
            ProductRepository productRepository,
            TradeRepository tradeRepository
    ) {
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
        this.reportRepository = reportRepository;
        this.productRepository = productRepository;
        this.tradeRepository = tradeRepository;
    }

    public AdminMeResponse getMe(AdminPrincipal admin) {
        validateAdmin(admin);
        Admin entity = adminRepository.findById(admin.getAdminId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Admin account not found."));
        return AdminMeResponse.from(entity);
    }

    public AdminDashboardResponse getDashboard(AdminPrincipal admin) {
        validateAdmin(admin);
        LocalDate today = AgoraClock.now().toLocalDate();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        return new AdminDashboardResponse(
                admin.getRole().name(),
                accessibleMenusFor(admin.getPermissions()),
                userRepository.count(),
                userRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end),
                reportRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end),
                tradeRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end),
                productRepository.countByApprovalStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        ProductApprovalStatus.PENDING,
                        start,
                        end
                ),
                productRepository.countByApprovalStatus(ProductApprovalStatus.APPROVED),
                reportRepository.findTop5ByStatusOrderByCreatedAtAsc(ReportStatus.PENDING).stream()
                        .map(this::toPendingReportResponse)
                        .toList()
        );
    }

    private PendingReportResponse toPendingReportResponse(Report report) {
        Product product = report.getProduct();
        User reportedUser = report.getReportedUser();
        String type = product == null ? "USER" : "PRODUCT";
        String target = "-";
        if (product != null) {
            target = product.getTitle();
        } else if (reportedUser != null) {
            target = reportedUser.getNickname();
        }

        return new PendingReportResponse(
                report.getId(),
                target,
                report.getReason(),
                report.getCreatedAt(),
                report.getStatus().name(),
                type
        );
    }

    private void validateAdmin(AdminPrincipal admin) {
        if (admin == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    private List<String> accessibleMenusFor(Set<AdminPermission> permissions) {
        List<String> menus = new ArrayList<>();
        if (permissions.contains(AdminPermission.USER_MANAGE)) {
            menus.add("USERS");
        }
        if (permissions.contains(AdminPermission.REPORT_MANAGE)) {
            menus.add("USER_REPORTS");
            menus.add("PRODUCT_REPORTS");
        }
        if (permissions.contains(AdminPermission.PRODUCT_MANAGE)) {
            menus.add("PRODUCTS");
        }
        if (permissions.contains(AdminPermission.PAYMENT_MANAGE)) {
            menus.add("PAYMENTS");
            menus.add("REFUNDS");
            menus.add("SETTLEMENTS");
        }
        if (permissions.contains(AdminPermission.COUPON_MANAGE)) {
            menus.add("COUPONS");
        }
        return List.copyOf(menus);
    }
}
