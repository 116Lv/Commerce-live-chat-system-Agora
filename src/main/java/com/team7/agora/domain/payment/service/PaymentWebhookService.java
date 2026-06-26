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

/**
 * 결제 웹훅 관련 비즈니스 유스케이스를 처리하는 서비스이다.
 */
@Service
@Transactional(readOnly = true)
public class PaymentWebhookService {

    private final PaymentRepository paymentRepository;
    private final SettlementRepository settlementRepository;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param paymentRepository 데이터를 조회하고 저장하는 리포지토리
     * @param settlementRepository 데이터를 조회하고 저장하는 리포지토리
     */
    public PaymentWebhookService(PaymentRepository paymentRepository, SettlementRepository settlementRepository) {
        this.paymentRepository = paymentRepository;
        this.settlementRepository = settlementRepository;
    }

    /**
     * 'handlePaid' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     * @param orderId 주문 ID
     * @param paymentKey 결제 승인 키
     * @return 클라이언트에 반환할 API 응답
     */
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
