package com.team7.agora.domain.nego.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;

class NegoExpirationSchedulerTest {

    @Test
    void expirationSchedulerUsesConfiguredDelayAndInitialDelay() throws NoSuchMethodException {
        Method method = NegoExpirationScheduler.class.getDeclaredMethod("expireDueOffersAndReservations");
        Scheduled scheduled = method.getAnnotation(Scheduled.class);

        assertThat(scheduled.fixedDelayString())
            .isEqualTo("${agora.scheduler.nego-expiration.fixed-delay:300000}");
        assertThat(scheduled.initialDelayString())
            .isEqualTo("${agora.scheduler.nego-expiration.initial-delay:300000}");
    }
}
