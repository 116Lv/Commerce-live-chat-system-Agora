package com.team7.agora.domain.payment.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.team7.agora.domain.payment.service.PaymentConfirmationRecoveryService;
import com.team7.agora.global.time.AgoraClock;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentConfirmationRecoverySchedulerTest {

    @Mock
    private PaymentConfirmationRecoveryService recoveryService;

    @Test
    void delegatesRecoveryWithTimeoutThreshold() {
        PaymentConfirmationRecoveryScheduler scheduler = new PaymentConfirmationRecoveryScheduler(recoveryService, 10);
        LocalDateTime before = AgoraClock.now().minusMinutes(10);

        scheduler.recoverStaleConfirmingPayments();

        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(recoveryService).recoverStaleConfirmingPayments(captor.capture());
        LocalDateTime after = AgoraClock.now().minusMinutes(10);
        assertThat(captor.getValue()).isBetween(before.minusSeconds(1), after.plusSeconds(1));
    }
}
