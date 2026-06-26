package com.team7.agora.domain.chat.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;

class ChatMessageRetentionSchedulerTest {

    @Test
    void retentionSchedulerUsesConfiguredCron() throws NoSuchMethodException {
        Method method = ChatMessageRetentionScheduler.class.getDeclaredMethod("deleteExpiredMessages");
        Scheduled scheduled = method.getAnnotation(Scheduled.class);

        assertThat(scheduled.cron())
            .isEqualTo("${agora.scheduler.chat-message-retention.cron:0 0 4 * * *}");
    }
}
