package com.team7.agora.domain.payment.service;

import com.team7.agora.domain.payment.entity.Payment;
import com.team7.agora.domain.payment.enums.PaymentStatus;
import com.team7.agora.domain.payment.repository.PaymentRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentConfirmationRecoveryService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public int recoverStaleConfirmingPayments(LocalDateTime threshold) {
        List<Payment> payments = paymentRepository.findAllByStatusAndConfirmingAtLessThanEqualForUpdate(
            PaymentStatus.CONFIRMING,
            threshold
        );
        payments.forEach(Payment::restoreReadyFromConfirming);
        return payments.size();
    }
}
