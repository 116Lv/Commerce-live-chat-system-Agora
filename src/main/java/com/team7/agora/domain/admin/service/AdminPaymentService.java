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

@Service
@Transactional(readOnly = true)
public class AdminPaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentClient paymentClient;

    public AdminPaymentService(PaymentRepository paymentRepository, PaymentClient paymentClient) {
        this.paymentRepository = paymentRepository;
        this.paymentClient = paymentClient;
    }

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

    public List<AdminPaymentResponse> getRefunds(CustomUserDetails admin) {
        validateSettlementAdmin(admin);
        return paymentRepository.findAllByStatus(PaymentStatus.REFUNDED).stream()
            .map(AdminPaymentResponse::from)
            .toList();
    }

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
