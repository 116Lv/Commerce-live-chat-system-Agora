// 채팅 메시지 보관 기간(3개월)이 지난 메시지를 정리하는 서비스
package com.team7.agora.domain.chat.service;

import com.team7.agora.domain.chat.repository.ChatMessageRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 보관 기간이 지난 채팅 메시지를 삭제하는 애플리케이션 서비스이다.
 */
@Service
public class ChatMessageRetentionService {

    private static final int RETENTION_MONTHS = 3;

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageRetentionService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    /**
     * 보관 기준 시각보다 오래된 채팅 메시지를 삭제한다.
     * @param now 현재 시각
     * @return 삭제된 메시지 수
     */
    @Transactional
    public long deleteMessagesOlderThanRetentionPeriod(LocalDateTime now) {
        LocalDateTime threshold = now.minusMonths(RETENTION_MONTHS);
        return chatMessageRepository.deleteByCreatedAtBefore(threshold);
    }
}
