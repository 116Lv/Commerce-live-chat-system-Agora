package com.team7.agora.domain.admin.service;

import com.team7.agora.domain.admin.dto.response.AdminApprovalRequestResponse;
import com.team7.agora.domain.admin.dto.response.AdminPaymentResponse;
import com.team7.agora.domain.admin.entity.Admin;
import com.team7.agora.domain.admin.entity.AdminApprovalRequest;
import com.team7.agora.domain.admin.enums.AdminPermission;
import com.team7.agora.domain.admin.repository.AdminApprovalRequestRepository;
import com.team7.agora.domain.admin.repository.AdminRepository;
import com.team7.agora.domain.payment.entity.Payment;
import com.team7.agora.domain.payment.enums.PaymentStatus;
import com.team7.agora.domain.payment.repository.PaymentRepository;
import com.team7.agora.domain.payment.service.PaymentService;
import com.team7.agora.domain.settlement.repository.SettlementRepository;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자 결제 관련 비즈니스 유스케이스를 처리하는 서비스이다.
 */
@Service
@Transactional(readOnly = true)
public class AdminPaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final SettlementRepository settlementRepository;
    private final AdminRepository adminRepository;
    private final AdminApprovalRequestRepository approvalRequestRepository;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param paymentRepository 데이터를 조회하고 저장하는 리포지토리
     * @param paymentService 결제 검증 및 확정 흐름을 처리하는 서비스
     * @param settlementRepository 정산 데이터를 조회하는 리포지토리
     * @param adminRepository 관리자 데이터를 조회하는 리포지토리
     * @param approvalRequestRepository 승인요청 데이터를 조회하고 저장하는 리포지토리
     */
    public AdminPaymentService(
        PaymentRepository paymentRepository,
        PaymentService paymentService,
        SettlementRepository settlementRepository,
        AdminRepository adminRepository,
        AdminApprovalRequestRepository approvalRequestRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
        this.settlementRepository = settlementRepository;
        this.adminRepository = adminRepository;
        this.approvalRequestRepository = approvalRequestRepository;
    }

    /**
     * 'getPayments' 메서드는 필요한 데이터를 조회해 호출한 쪽에 반환한다.
     * @param admin 인증된 관리자 정보
     * @param status 조회 또는 변경할 상태
     * @param pageable 페이지 요청 정보
     * @return 클라이언트에 반환할 API 응답
     */
    public List<AdminPaymentResponse> getPayments(
        AdminPrincipal admin,
        PaymentStatus status,
        Pageable pageable
    ) {
        validateSettlementAdmin(admin);
        if (status != null) {
            return paymentRepository.findAllByStatus(status, pageable).stream()
                .map(this::toResponse)
                .toList();
        }
        return paymentRepository.findAll(pageable).stream()
            .map(this::toResponse)
            .toList();
    }

    /**
     * 'getRefunds' 메서드는 필요한 데이터를 조회해 호출한 쪽에 반환한다.
     * @param admin 인증된 관리자 정보
     * @return 클라이언트에 반환할 API 응답
     */
    public List<AdminPaymentResponse> getRefunds(AdminPrincipal admin) {
        validateSettlementAdmin(admin);
        return paymentRepository.findAllByStatus(PaymentStatus.REFUNDED).stream()
            .map(this::toResponse)
            .toList();
    }

    /**
     * 'verifyPayment' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     * @param admin 인증된 관리자 정보
     * @param paymentId 결제 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public AdminPaymentResponse verifyPayment(AdminPrincipal admin, Long paymentId) {
        validateSettlementAdmin(admin);
        Payment payment = findPayment(paymentId);

        String paymentKey = payment.getPaymentKey() == null ? payment.getOrderId() : payment.getPaymentKey();
        paymentService.confirmByPaymentId(paymentId, paymentKey);
        return toResponse(findPayment(paymentId));
    }

    /**
     * 구매자가 환불을 신청한 결제 목록을 조회한다.
     * @param admin 인증된 관리자 정보
     * @return 클라이언트에 반환할 API 응답
     */
    public List<AdminPaymentResponse> getRefundRequests(AdminPrincipal admin) {
        validateSettlementAdmin(admin);
        return paymentRepository.findAllByStatusAndRefundRequestedAtIsNotNull(PaymentStatus.PAID).stream()
            .map(this::toResponse)
            .toList();
    }

    /**
     * 구매자의 환불 신청을 승인요청(AdminApprovalRequest)으로 접수한다.
     * 실제 환불은 별도의 승인 절차(POST /api/admin/approval-requests/{id}/approve)를 거쳐야 처리된다.
     * @param admin 인증된 관리자 정보
     * @param paymentId 결제 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @Transactional
    public AdminApprovalRequestResponse requestRefundApproval(AdminPrincipal admin, Long paymentId) {
        validateSettlementAdmin(admin);
        Payment payment = findPayment(paymentId);
        if (payment.getStatus() != PaymentStatus.PAID || payment.getRefundRequestedAt() == null) {
            throw new BusinessException(ErrorCode.CONFLICT, "구매자가 환불을 신청한 결제 완료 건만 승인요청할 수 있습니다.");
        }

        Admin requester = getCurrentAdmin(admin);
        String pendingRequestKey = AdminApprovalRequest.buildPaymentRefundPendingRequestKey(paymentId);
        if (approvalRequestRepository.existsByPendingRequestKey(pendingRequestKey)) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 대기 중인 환불 승인요청이 있습니다.");
        }

        AdminApprovalRequest request = AdminApprovalRequest.createPaymentRefund(requester, paymentId, payment.getRefundReason());
        try {
            return AdminApprovalRequestResponse.from(approvalRequestRepository.save(request));
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 대기 중인 환불 승인요청이 있습니다.");
        }
    }

    private Admin getCurrentAdmin(AdminPrincipal admin) {
        if (admin == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return adminRepository.findById(admin.getAdminId())
            .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Admin account not found."));
    }

    private Payment findPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "결제를 찾을 수 없습니다."));
    }

    private AdminPaymentResponse toResponse(Payment payment) {
        return settlementRepository.findByPayment(payment)
            .map(settlement -> AdminPaymentResponse.from(payment, settlement.getId(), settlement.getStatus().name()))
            .orElseGet(() -> AdminPaymentResponse.from(payment));
    }

    private void validateSettlementAdmin(AdminPrincipal admin) {
        if (!AdminRoleSupport.hasPermission(admin, AdminPermission.PAYMENT_MANAGE)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "결제 관리는 정산 관리자만 수행할 수 있습니다.");
        }
    }
}
