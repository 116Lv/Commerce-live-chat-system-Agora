package com.team7.agora.domain.chat.entity;

import com.team7.agora.domain.chat.enums.ChatMessageType;
import com.team7.agora.domain.common.entity.BaseTimeEntity;
import com.team7.agora.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "chat_messages",
    indexes = @Index(name = "idx_chat_messages_room_id", columnList = "chat_room_id, id")
)
public class ChatMessage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, length = 1000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatMessageType messageType;

    private ChatMessage(ChatRoom chatRoom, User sender, String content, ChatMessageType messageType) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.content = content;
        this.messageType = messageType;
        markCreatedNow();
    }

    public static ChatMessage send(ChatRoom chatRoom, User sender, String content) {
        return new ChatMessage(chatRoom, sender, content, ChatMessageType.TEXT);
    }

    public static ChatMessage sendImage(ChatRoom chatRoom, User sender, String imageUrl) {
        return new ChatMessage(chatRoom, sender, imageUrl, ChatMessageType.IMAGE);
    }

    public static ChatMessage system(ChatRoom chatRoom, User actor, String content) {
        return new ChatMessage(chatRoom, actor, content, ChatMessageType.SYSTEM);
    }
}
