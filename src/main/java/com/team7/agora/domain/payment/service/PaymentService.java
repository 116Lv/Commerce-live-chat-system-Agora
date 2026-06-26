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
import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 결제 관련 비즈니스 유스케이스를 처리하는 서비스이다.
 */
@Service
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SettlementRepository settlementRepository;
    private final TradeRepository tradeRepository;
    private final PaymentClient paymentClient;
    private final TransactionOperations transactionOperations;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param paymentRepository 데이터를 조회하고 저장하는 리포지토리
     * @param settlementRepository 데이터를 조회하고 저장하는 리포지토리
     * @param tradeRepository 데이터를 조회하고 저장하는 리포지토리
     * @param paymentClient 외부 시스템 또는 저장소와 통신하는 클라이언트
     */
    @Autowired
    public PaymentService(
        PaymentRepository paymentRepository,
        SettlementRepository settlementRepository,
        TradeRepository tradeRepository,
        PaymentClient paymentClient,
        PlatformTransactionManager transactionManager
    ) {
        this(paymentRepository, settlementRepository, tradeRepository, paymentClient, new TransactionTemplate(transactionManager));
    }

    PaymentService(
        PaymentRepository paymentRepository,
        SettlementRepository settlementRepository,
        TradeRepository tradeRepository,
        PaymentClient paymentClient,
        TransactionOperations transactionOperations
    ) {
        this.paymentRepository = paymentRepository;
        this.settlementRepository = settlementRepository;
        this.tradeRepository = tradeRepository;
        this.paymentClient = paymentClient;
        this.transactionOperations = transactionOperations;
    }

    /**
     * 거래 정보를 확인하고 결제 대기 상태의 결제 데이터를 생성한다.
     * @param payerId 결제자 ID
     * @param tradeId 거래 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @Transactional
    public PaymentResponse prepare(Long payerId, Long tradeId) {
        Trade trade = findTradeForUpdate(tradeId);

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
        try {
            return PaymentResponse.from(paymentRepository.save(payment));
        } catch (DataIntegrityViolationException e) {
            throw new PaymentException(ErrorCode.CONFLICT, "이미 결제가 생성된 거래입니다.");
        }
    }

    /**
     * 외부 결제 승인 결과를 검증하고 결제를 완료 상태로 변경한다.
     * @param payerId 결제자 ID
     * @param paymentId 결제 ID
     * @param paymentKey 결제 승인 키
     * @return 클라이언트에 반환할 API 응답
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public PaymentResponse confirm(Long payerId, Long paymentId, String paymentKey) {
        ConfirmationAttempt attempt = reserveConfirmationById(paymentId, payment -> payment.validatePayer(payerId));
        return confirmPayment(attempt, paymentKey);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public PaymentResponse confirmByPaymentId(Long paymentId, String paymentKey) {
        return confirmPayment(reserveConfirmationById(paymentId, payment -> { }), paymentKey);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public PaymentResponse confirmByOrderId(String orderId, String paymentKey) {
        return confirmPayment(reserveConfirmationByOrderId(orderId), paymentKey);
    }

    private PaymentResponse confirmPayment(ConfirmationAttempt attempt, String paymentKey) {
        if (attempt.alreadyConfirmed() != null) {
            return attempt.alreadyConfirmed();
        }

        boolean approved = paymentClient.confirm(paymentKey, attempt.orderId(), attempt.amount());
        if (!approved) {
            markConfirmationFailed(attempt.paymentId());
            throw new PaymentException(ErrorCode.INVALID_REQUEST, "결제 승인이 거절되었습니다.");
        }

        return transactionOperations.execute(status -> {
            Payment payment = findPaymentForUpdate(attempt.paymentId());
            if (payment.getStatus() == PaymentStatus.PAID) {
                return PaymentResponse.from(payment);
            }
            payment.markPaid(paymentKey);
            payment.getTrade().markPaid();
            Settlement settlement = settlementRepository.save(Settlement.pending(payment));
            return PaymentResponse.from(payment, settlement.getId());
        });
    }

    private ConfirmationAttempt reserveConfirmationById(Long paymentId, Consumer<Payment> validator) {
        return transactionOperations.execute(status -> {
            Payment payment = findPaymentForUpdate(paymentId);
            validator.accept(payment);
            return reserveConfirmation(payment);
        });
    }

    private ConfirmationAttempt reserveConfirmationByOrderId(String orderId) {
        return transactionOperations.execute(status -> {
            Payment payment = paymentRepository.findByOrderIdForUpdate(orderId)
                .orElseThrow(() -> new PaymentException(ErrorCode.NOT_FOUND, "결제를 찾을 수 없습니다."));
            return reserveConfirmation(payment);
        });
    }

    private ConfirmationAttempt reserveConfirmation(Payment payment) {
        if (payment.getStatus() == PaymentStatus.PAID) {
            return new ConfirmationAttempt(payment.getId(), payment.getOrderId(), payment.getAmount(), PaymentResponse.from(payment));
        }
        payment.markConfirming();
        return new ConfirmationAttempt(payment.getId(), payment.getOrderId(), payment.getAmount(), null);
    }

    private void markConfirmationFailed(Long paymentId) {
        transactionOperations.execute(status -> {
            Payment payment = findPaymentForUpdate(paymentId);
            payment.markFailed();
            return null;
        });
    }

    private record ConfirmationAttempt(
        Long paymentId,
        String orderId,
        BigDecimal amount,
        PaymentResponse alreadyConfirmed
    ) {
    }

    /**
     * 결제 환불 요청을 검증하고 결제 상태를 환불 처리로 갱신한다.
     * @param payerId 결제자 ID
     * @param paymentId 결제 ID
     * @param reason 처리 사유
     * @return 클라이언트에 반환할 API 응답
     */
    @Transactional
    public PaymentResponse refund(Long payerId, Long paymentId, String reason) {
        Payment payment = findPayment(paymentId);
        payment.validatePayer(payerId);
        payment.refund(reason);
        
        settlementRepository.findByPaymentTradeId(payment.getTrade().getId())
            .ifPresent(Settlement::cancel);

        return PaymentResponse.from(payment);
    }

    /**
     * 결제 ID로 현재 환불 처리 상태를 조회한다.
     * @param authUser 인증 사용자 정보
     * @param paymentId 결제 ID
     * @return 클라이언트에 반환할 API 응답
     */
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

    private Payment findPaymentForUpdate(Long paymentId) {
        return paymentRepository.findByIdForUpdate(paymentId)
            .orElseThrow(() -> new PaymentException(ErrorCode.NOT_FOUND, "결제를 찾을 수 없습니다."));
    }

    private Trade findTrade(Long tradeId) {
        return tradeRepository.findById(tradeId)
            .orElseThrow(() -> new PaymentException(ErrorCode.NOT_FOUND, "거래를 찾을 수 없습니다."));
    }

    private Trade findTradeForUpdate(Long tradeId) {
        return tradeRepository.findByIdForUpdate(tradeId)
            .orElseThrow(() -> new PaymentException(ErrorCode.NOT_FOUND, "거래를 찾을 수 없습니다."));
    }
}
