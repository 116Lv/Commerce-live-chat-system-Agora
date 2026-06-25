package com.team7.agora.domain.chat.repository;

import com.team7.agora.domain.chat.entity.ChatMessage;
import com.team7.agora.domain.chat.entity.ChatRoom;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("select m from ChatMessage m join fetch m.sender where m.chatRoom = :chatRoom order by m.id desc")
    List<ChatMessage> findLatestByChatRoom(@Param("chatRoom") ChatRoom chatRoom, Pageable pageable);

    @Query("select m from ChatMessage m join fetch m.sender "
        + "where m.chatRoom = :chatRoom and m.id < :lastMessageId order by m.id desc")
    List<ChatMessage> findByChatRoomBeforeMessageId(
        @Param("chatRoom") ChatRoom chatRoom,
        @Param("lastMessageId") Long lastMessageId,
        Pageable pageable
    );

    long countByCreatedAtBefore(LocalDateTime threshold);

    void deleteByCreatedAtBefore(LocalDateTime threshold);
}
