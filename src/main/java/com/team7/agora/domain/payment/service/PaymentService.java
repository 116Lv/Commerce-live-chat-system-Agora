package com.team7.agora.domain.payment.service;

import com.team7.agora.domain.payment.client.PaymentClient;
import com.team7.agora.domain.payment.dto.response.PaymentResponse;
import com.team7.agora.domain.payment.dto.response.RefundStatusResponse;
import com.team7.agora.domain.payment.entity.Payment;
import com.team7.agora.domain.payment.enums.PaymentStatus;
import com.team7.agora.domain.payment.exception.PaymentException;
import com.team7.agora.domain.payment.repository.PaymentRepository;
import com.team7.agora.domain.product.enums.ProductStatus;
import com.team7.agora.domain.settlement.entity.Settlement;
import com.team7.agora.domain.settlement.repository.SettlementRepository;
import com.team7.agora.domain.trade.entity.Trade;
import com.team7.agora.domain.trade.enums.TradeStatus;
import com.team7.agora.domain.trade.repository.TradeRepository;
import com.team7.agora.global.auth.AuthUser;
import com.team7.agora.global.exception.ErrorCode;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SettlementRepository settlementRepository;
    private final TradeRepository tradeRepository;
    private final PaymentClient paymentClient;

    public PaymentService(
        PaymentRepository paymentRepository,
        SettlementRepository settlementRepository,
        TradeRepository tradeRepository,
        PaymentClient paymentClient
    ) {
        this.paymentRepository = paymentRepository;
        this.settlementRepository = settlementRepository;
        this.tradeRepository = tradeRepository;
        this.paymentClient = paymentClient;
    }

    @Transactional
    public PaymentResponse prepare(Long payerId, Long tradeId) {
        Trade trade = findTrade(tradeId);

        if (!trade.getBuyer().getId().equals(payerId)) {
            throw new PaymentException(ErrorCode.FORBIDDEN, "구매자만 결제를 준비할 수 있습니다.");
        }
        if (trade.getStatus() != TradeStatus.PAYMENT_PENDING) {
            throw new PaymentException(ErrorCode.INVALID_REQUEST, "결제 대기 중인 거래만 결제할 수 있습니다.");
        }
        if (trade.getProduct().getStatus() != ProductStatus.RESERVED) {
            throw new PaymentException(ErrorCode.INVALID_REQUEST, "예약중인 상품만 결제할 수 있습니다.");
        }
        if (paymentRepository.existsByTrade(trade)) {
            throw new PaymentException(ErrorCode.CONFLICT, "이미 결제가 생성된 거래입니다.");
        }

        Payment payment = Payment.ready(
            trade,
            trade.getBuyer(),
            trade.getPrice(),
            "order-" + UUID.randomUUID()
        );
        return PaymentResponse.from(paymentRepository.save(payment));
    }

    @Transactional
    public PaymentResponse confirm(Long payerId, Long paymentId, String paymentKey) {
        Payment payment = findPayment(paymentId);
        payment.validatePayer(payerId);
        if (payment.getStatus() == PaymentStatus.PAID) {
            return PaymentResponse.from(payment);
        }

        boolean approved = paymentClient.confirm(paymentKey, payment.getOrderId(), payment.getAmount());
        if (!approved) {
            throw new PaymentException(ErrorCode.INVALID_REQUEST, "결제 승인이 거절되었습니다.");
        }

        payment.markPaid(paymentKey);
        payment.getTrade().markPaid();
        Settlement settlement = settlementRepository.save(Settlement.pending(payment));
        return PaymentResponse.from(payment, settlement.getId());
    }

    @Transactional
    public PaymentResponse refund(Long payerId, Long paymentId, String reason) {
        Payment payment = findPayment(paymentId);
        payment.validatePayer(payerId);
        payment.refund(reason);
        
        settlementRepository.findByPaymentTradeId(payment.getTrade().getId())
            .ifPresent(Settlement::cancel);

        return PaymentResponse.from(payment);
    }

    public RefundStatusResponse getRefundStatus(AuthUser authUser, Long paymentId) {
        Payment payment = findPayment(paymentId);

        if (!hasRefundStatusAccess(authUser, payment)) {
            throw new PaymentException(ErrorCode.FORBIDDEN, "결제 참여자 또는 정산 관리자만 환불 상태를 조회할 수 있습니다.");
        }

        return RefundStatusResponse.from(payment);
    }

    private boolean hasRefundStatusAccess(AuthUser authUser, Payment payment) {
        if (authUser == null) {
            return false;
        }
        if ("ROOT_ADMIN".equals(authUser.role()) || "SETTLEMENT_ADMIN".equals(authUser.role())) {
            return true;
        }
        return payment.getTrade().isParticipant(authUser.userId());
    }

    private Payment findPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentException(ErrorCode.NOT_FOUND, "결제를 찾을 수 없습니다."));
    }

    private Trade findTrade(Long tradeId) {
        return tradeRepository.findById(tradeId)
            .orElseThrow(() -> new PaymentException(ErrorCode.NOT_FOUND, "거래를 찾을 수 없습니다."));
    }
}
