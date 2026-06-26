// 매일 새벽 보관 기간이 지난 채팅 메시지를 정리하는 스케줄러
package com.team7.agora.domain.chat.scheduler;

import com.team7.agora.domain.chat.service.ChatMessageRetentionService;
import com.team7.agora.global.time.AgoraClock;
import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 설정된 보관 기간이 지난 채팅 메시지를 정리하는 스케줄러이다.
 */
@Component
public class ChatMessageRetentionScheduler {

    private final ChatMessageRetentionService chatMessageRetentionService;

    public ChatMessageRetentionScheduler(ChatMessageRetentionService chatMessageRetentionService) {
        this.chatMessageRetentionService = chatMessageRetentionService;
    }

    /**
     * 채팅 메시지 보관 기간 정리 작업을 실행한다.
     */
    @Scheduled(cron = "${agora.scheduler.chat-message-retention.cron:0 0 4 * * *}")
    public void deleteExpiredMessages() {
        chatMessageRetentionService.deleteMessagesOlderThanRetentionPeriod(AgoraClock.now());
    }
}
