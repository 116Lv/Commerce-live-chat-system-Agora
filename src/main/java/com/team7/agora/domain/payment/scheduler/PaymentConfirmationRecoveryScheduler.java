package com.team7.agora.domain.payment.scheduler;

import com.team7.agora.domain.payment.service.PaymentConfirmationRecoveryService;
import com.team7.agora.global.time.AgoraClock;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PaymentConfirmationRecoveryScheduler {

    private final PaymentConfirmationRecoveryService recoveryService;
    private final long timeoutMinutes;

    public PaymentConfirmationRecoveryScheduler(
        PaymentConfirmationRecoveryService recoveryService,
        @Value("${agora.payment.confirmation-timeout-minutes:10}") long timeoutMinutes
    ) {
        this.recoveryService = recoveryService;
        this.timeoutMinutes = timeoutMinutes;
    }

    @Scheduled(
        fixedDelayString = "${agora.scheduler.payment-confirmation-recovery.fixed-delay:300000}",
        initialDelayString = "${agora.scheduler.payment-confirmation-recovery.initial-delay:300000}"
    )
    public void recoverStaleConfirmingPayments() {
        LocalDateTime threshold = AgoraClock.now().minusMinutes(timeoutMinutes);
        recoveryService.recoverStaleConfirmingPayments(threshold);
    }
}
