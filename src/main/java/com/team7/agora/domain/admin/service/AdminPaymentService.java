package com.team7.agora.domain.admin.service;

import com.team7.agora.domain.admin.dto.response.AdminPaymentResponse;
import com.team7.agora.domain.payment.client.PaymentClient;
import com.team7.agora.domain.payment.entity.Payment;
import com.team7.agora.domain.payment.enums.PaymentStatus;
import com.team7.agora.domain.payment.repository.PaymentRepository;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 애플리케이션 유스케이스를 조정하는 서비스이다.
 */
@Service
@Transactional(readOnly = true)
public class AdminPaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentClient paymentClient;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param paymentRepository 입력 값
     * @param paymentClient 입력 값
     */
    public AdminPaymentService(PaymentRepository paymentRepository, PaymentClient paymentClient) {
        this.paymentRepository = paymentRepository;
        this.paymentClient = paymentClient;
    }

    /**
     * 데이터를 반환한다.
     * @param admin 입력 값
     * @param status 입력 값
     * @param pageable 입력 값
     * @return 처리 결과
     */
    public List<AdminPaymentResponse> getPayments(
        CustomUserDetails admin,
        PaymentStatus status,
        Pageable pageable
    ) {
        validateSettlementAdmin(admin);
        if (status != null) {
            return paymentRepository.findAllByStatus(status, pageable).stream()
                .map(AdminPaymentResponse::from)
                .toList();
        }
        return paymentRepository.findAll(pageable).stream()
            .map(AdminPaymentResponse::from)
            .toList();
    }

    /**
     * 데이터를 반환한다.
     * @param admin 입력 값
     * @return 처리 결과
     */
    public List<AdminPaymentResponse> getRefunds(CustomUserDetails admin) {
        validateSettlementAdmin(admin);
        return paymentRepository.findAllByStatus(PaymentStatus.REFUNDED).stream()
            .map(AdminPaymentResponse::from)
            .toList();
    }

    /**
     * 요청한 동작을 처리한다.
     * @param admin 입력 값
     * @param paymentId 입력 값
     * @return 처리 결과
     */
    @Transactional
    public AdminPaymentResponse verifyPayment(CustomUserDetails admin, Long paymentId) {
        validateSettlementAdmin(admin);
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "결제를 찾을 수 없습니다."));

        if (payment.getPaymentKey() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "결제 키가 없는 결제는 재검증할 수 없습니다.");
        }

        boolean approved = paymentClient.confirm(payment.getPaymentKey(), payment.getOrderId(), payment.getAmount());
        if (approved) {
            payment.markPaid(payment.getPaymentKey());
        } else if (payment.getStatus() != PaymentStatus.FAILED) {
            payment.markFailed();
        }
        return AdminPaymentResponse.from(payment);
    }

    private void validateSettlementAdmin(CustomUserDetails admin) {
        if (admin == null || !AdminRoleSupport.isSettlementAdminRole(admin.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "결제 관리는 정산 관리자만 수행할 수 있습니다.");
        }
    }
}
