// 채팅 메시지 보관 기간(3개월)이 지난 메시지를 정리하는 서비스
package com.team7.agora.domain.chat.service;

import com.team7.agora.domain.chat.repository.ChatMessageRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatMessageRetentionService {

    private static final int RETENTION_MONTHS = 3;

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageRetentionService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    @Transactional
    public long deleteMessagesOlderThanRetentionPeriod(LocalDateTime now) {
        LocalDateTime threshold = now.minusMonths(RETENTION_MONTHS);
        long deletedCount = chatMessageRepository.countByCreatedAtBefore(threshold);
        chatMessageRepository.deleteByCreatedAtBefore(threshold);
        return deletedCount;
    }
}
