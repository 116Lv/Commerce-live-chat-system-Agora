package com.team7.agora.domain.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.chat.repository.ChatMessageRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatMessageRetentionServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Test
    void deleteMessagesOlderThanRetentionPeriod_deletesMessagesBeforeThreeMonthThreshold() {
        ChatMessageRetentionService service = new ChatMessageRetentionService(chatMessageRepository);
        LocalDateTime now = LocalDateTime.of(2026, 6, 26, 0, 0);
        LocalDateTime expectedThreshold = now.minusMonths(3);
        when(chatMessageRepository.countByCreatedAtBefore(expectedThreshold)).thenReturn(7L);

        long deletedCount = service.deleteMessagesOlderThanRetentionPeriod(now);

        assertThat(deletedCount).isEqualTo(7L);
        verify(chatMessageRepository).deleteByCreatedAtBefore(expectedThreshold);
    }
}
