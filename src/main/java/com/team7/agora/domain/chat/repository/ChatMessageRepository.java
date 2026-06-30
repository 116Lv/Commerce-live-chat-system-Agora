package com.team7.agora.domain.chat.repository;

import com.team7.agora.domain.chat.entity.ChatMessage;
import com.team7.agora.domain.chat.entity.ChatRoom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Chat Message 데이터 저장과 조회를 담당하는 저장소 인터페이스이다.
 */
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @EntityGraph(attributePaths = "sender")
    Optional<ChatMessage> findFirstByChatRoomOrderByIdDesc(ChatRoom chatRoom);

    @Query("""
        select m
        from ChatMessage m
        join fetch m.sender
        where m.chatRoom.id in :chatRoomIds
          and m.id = (
            select max(latest.id)
            from ChatMessage latest
            where latest.chatRoom = m.chatRoom
          )
        """)
    List<ChatMessage> findLatestMessagesByChatRoomIds(@Param("chatRoomIds") List<Long> chatRoomIds);

    @Query("select m from ChatMessage m join fetch m.sender where m.chatRoom = :chatRoom order by m.id desc")
    List<ChatMessage> findLatestByChatRoom(@Param("chatRoom") ChatRoom chatRoom, Pageable pageable);

    @Query("select m from ChatMessage m join fetch m.sender "
        + "where m.chatRoom = :chatRoom and m.id < :lastMessageId order by m.id desc")
    List<ChatMessage> findByChatRoomBeforeMessageId(
        @Param("chatRoom") ChatRoom chatRoom,
        @Param("lastMessageId") Long lastMessageId,
        Pageable pageable
    );

    @Modifying
    @Query("delete from ChatMessage m where m.createdAt < :threshold")
    int deleteByCreatedAtBefore(@Param("threshold") LocalDateTime threshold);

    @Query("""
        select count(m)
        from ChatMessage m
        where m.chatRoom = :chatRoom
          and m.sender.id <> :userId
          and (:lastReadAt is null or m.createdAt > :lastReadAt)
        """)
    long countUnreadMessagesForUser(
        @Param("chatRoom") ChatRoom chatRoom,
        @Param("userId") Long userId,
        @Param("lastReadAt") LocalDateTime lastReadAt
    );

    @Query("""
        select new com.team7.agora.domain.chat.repository.ChatRoomUnreadCount(m.chatRoom.id, count(m))
        from ChatMessage m
        join m.chatRoom cr
        where cr.id in :chatRoomIds
          and m.sender.id <> :userId
          and (
            (cr.seller.id = :userId and (cr.sellerLastReadAt is null or m.createdAt > cr.sellerLastReadAt))
            or (cr.buyer.id = :userId and (cr.buyerLastReadAt is null or m.createdAt > cr.buyerLastReadAt))
          )
        group by m.chatRoom.id
        """)
    List<ChatRoomUnreadCount> countUnreadMessagesByChatRoomIds(
        @Param("chatRoomIds") List<Long> chatRoomIds,
        @Param("userId") Long userId
    );
}
