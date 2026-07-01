package com.team7.agora.domain.admin.service;

import com.team7.agora.domain.admin.dto.response.AdminPaymentResponse;
import com.team7.agora.domain.admin.enums.AdminPermission;
import com.team7.agora.domain.payment.client.PaymentClient;
import com.team7.agora.domain.payment.entity.Payment;
import com.team7.agora.domain.payment.enums.PaymentStatus;
import com.team7.agora.domain.payment.repository.PaymentRepository;
import com.team7.agora.domain.payment.service.PaymentService;
import com.team7.agora.domain.settlement.entity.Settlement;
import com.team7.agora.domain.settlement.repository.SettlementRepository;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.util.List;
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
    private final PaymentClient paymentClient;
    private final PaymentService paymentService;
    private final SettlementRepository settlementRepository;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param paymentRepository 데이터를 조회하고 저장하는 리포지토리
     * @param paymentClient 외부 시스템 또는 저장소와 통신하는 클라이언트
     */
    public AdminPaymentService(
        PaymentRepository paymentRepository,
        PaymentClient paymentClient,
        PaymentService paymentService,
        SettlementRepository settlementRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentClient = paymentClient;
        this.paymentService = paymentService;
        this.settlementRepository = settlementRepository;
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

        if (payment.getPaymentKey() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "결제 키가 없는 결제는 재검증할 수 없습니다.");
        }

        paymentService.confirmByPaymentId(paymentId, payment.getPaymentKey());
        return toResponse(findPayment(paymentId));
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
