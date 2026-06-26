package com.team7.agora.domain.chat.entity;

import com.team7.agora.domain.chat.enums.ChatRoomStatus;
import com.team7.agora.domain.common.entity.BaseTimeEntity;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.global.time.AgoraClock;
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
 * 채팅방 도메인 정보를 영속화하는 JPA 엔티티이다.
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
     * 'open' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     * @param product 상품 엔티티
     * @param buyer 구매자 엔티티
     * @return 클라이언트에 반환할 API 응답
     */
    public static ChatRoom open(Product product, User buyer) {
        return new ChatRoom(product, product.getSeller(), buyer);
    }

    /**
     * 규칙을 검증한다.
     * @param userId 회원 ID
     */
    public void validateParticipant(Long userId) {
        if (!isParticipant(userId)) {
            throw new IllegalArgumentException("채팅방 참여자만 접근할 수 있습니다.");
        }
    }

    /**
     * 조건 충족 여부를 확인한다.
     * @param userId 회원 ID
     * @return 클라이언트에 반환할 API 응답
     */
    public boolean isParticipant(Long userId) {
        return seller.getId().equals(userId) || buyer.getId().equals(userId);
    }

    /**
     * 사용자가 채팅방 메시지를 읽은 시각을 갱신해 읽음 상태로 표시한다.
     * @param userId 회원 ID
     */
    public void markRead(Long userId) {
        if (seller.getId().equals(userId)) {
            this.sellerLastReadAt = AgoraClock.now();
        } else if (buyer.getId().equals(userId)) {
            this.buyerLastReadAt = AgoraClock.now();
        } else {
            throw new IllegalArgumentException("채팅방 참여자만 읽음 처리를 할 수 있습니다.");
        }
    }
}
