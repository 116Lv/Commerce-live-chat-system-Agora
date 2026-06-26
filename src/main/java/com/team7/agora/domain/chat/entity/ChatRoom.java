package com.team7.agora.domain.chat.entity;

import com.team7.agora.domain.chat.enums.ChatRoomStatus;
import com.team7.agora.domain.common.entity.BaseTimeEntity;
import com.team7.agora.domain.product.entity.Product;
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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "chat_rooms",
    uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "seller_id", "buyer_id"}),
    indexes = {
        @Index(name = "idx_chat_rooms_buyer_status", columnList = "buyer_id, status"),
        @Index(name = "idx_chat_rooms_seller_status", columnList = "seller_id, status")
    }
/**
 * JPA 엔티티이다.
 */
)
public class ChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatRoomStatus status;

    private LocalDateTime sellerLastReadAt;
    private LocalDateTime buyerLastReadAt;

    private ChatRoom(Product product, User seller, User buyer) {
        this.product = product;
        this.seller = seller;
        this.buyer = buyer;
        this.status = ChatRoomStatus.ACTIVE;
        markCreatedNow();
    }

    /**
     * 요청한 동작을 처리한다.
     * @param product 입력 값
     * @param buyer 입력 값
     * @return 처리 결과
     */
    public static ChatRoom open(Product product, User buyer) {
        return new ChatRoom(product, product.getSeller(), buyer);
    }

    /**
     * 규칙을 검증한다.
     * @param userId 입력 값
     */
    public void validateParticipant(Long userId) {
        if (!isParticipant(userId)) {
            throw new IllegalArgumentException("채팅방 참여자만 접근할 수 있습니다.");
        }
    }

    /**
     * 조건 충족 여부를 확인한다.
     * @param userId 입력 값
     * @return 처리 결과
     */
    public boolean isParticipant(Long userId) {
        return seller.getId().equals(userId) || buyer.getId().equals(userId);
    }

    /**
     * 상태를 변경한다.
     * @param userId 입력 값
     */
    public void markRead(Long userId) {
        if (seller.getId().equals(userId)) {
            this.sellerLastReadAt = LocalDateTime.now();
        } else if (buyer.getId().equals(userId)) {
            this.buyerLastReadAt = LocalDateTime.now();
        } else {
            throw new IllegalArgumentException("채팅방 참여자만 읽음 처리를 할 수 있습니다.");
        }
    }
}
