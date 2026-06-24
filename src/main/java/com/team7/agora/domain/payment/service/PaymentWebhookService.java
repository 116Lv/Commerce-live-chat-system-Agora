package com.team7.agora.domain.payment.service;

import com.team7.agora.domain.payment.dto.response.PaymentResponse;
import com.team7.agora.domain.payment.entity.Payment;
import com.team7.agora.domain.payment.enums.PaymentStatus;
import com.team7.agora.domain.payment.exception.PaymentException;
import com.team7.agora.domain.payment.repository.PaymentRepository;
import com.team7.agora.domain.settlement.entity.Settlement;
import com.team7.agora.domain.settlement.repository.SettlementRepository;
import com.team7.agora.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PaymentWebhookService {

    private final PaymentRepository paymentRepository;
    private final SettlementRepository settlementRepository;

    public PaymentWebhookService(PaymentRepository paymentRepository, SettlementRepository settlementRepository) {
        this.paymentRepository = paymentRepository;
        this.settlementRepository = settlementRepository;
    }

    @Transactional
    public PaymentResponse handlePaid(String orderId, String paymentKey) {
        Payment payment = paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new PaymentException(ErrorCode.NOT_FOUND, "결제를 찾을 수 없습니다."));

        if (settlementRepository.existsByPayment(payment)) {
            return PaymentResponse.from(payment);
        }

        if (payment.getStatus() != PaymentStatus.PAID) {
            payment.markPaid(paymentKey);
            payment.getTrade().markPaid();
        }

        Settlement settlement = settlementRepository.save(Settlement.pending(payment));
        return PaymentResponse.from(payment, settlement.getId());
    }
}
