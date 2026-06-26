package com.team7.agora.domain.nego.entity;

import com.team7.agora.domain.chat.entity.ChatRoom;
import com.team7.agora.domain.common.entity.BaseTimeEntity;
import com.team7.agora.domain.nego.enums.NegoOfferStatus;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Nego Offer 도메인 정보를 영속화하는 JPA 엔티티이다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "nego_offers",
    indexes = @Index(name = "idx_nego_offers_chat_room_status", columnList = "chat_room_id, status")
)
public class NegoOffer extends BaseTimeEntity {

    /**
     * 만료 검사에서 활성 상태로 판단할 네고 제안 상태를 정의한다.
     */
    public static final List<NegoOfferStatus> ACTIVE_STATUSES = List.of(
        NegoOfferStatus.PENDING,
        NegoOfferStatus.EXTENSION_REQUESTED,
        NegoOfferStatus.EXTENDED
    );

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal offerPrice;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NegoOfferStatus status;

    private LocalDateTime respondedAt;

    private NegoOffer(ChatRoom chatRoom, User requester, BigDecimal offerPrice) {
        this.chatRoom = chatRoom;
        this.requester = requester;
        this.offerPrice = offerPrice;
        this.status = NegoOfferStatus.PENDING;
        markCreatedNow();
        this.expiresAt = getCreatedAt().plusHours(24);
    }

    /**
     * 채팅방과 구매자, 제안 가격을 기준으로 새 네고 제안 엔티티를 생성한다.
     * @param chatRoom 채팅방 엔티티
     * @param requester 가격 제안을 만든 구매자 엔티티
     * @param offerPrice 제안 가격
     * @return 클라이언트에 반환할 API 응답
     */
    public static NegoOffer create(ChatRoom chatRoom, User requester, BigDecimal offerPrice) {
        return new NegoOffer(chatRoom, requester, offerPrice);
    }

    /**
     * 'accept' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     */
    public void accept() {
        validatePending();
        this.status = NegoOfferStatus.ACCEPTED;
        this.respondedAt = AgoraClock.now();
    }

    /**
     * 도메인 객체를 거절 상태로 변경한다.
     */
    public void reject() {
        validatePending();
        this.status = NegoOfferStatus.REJECTED;
        this.respondedAt = AgoraClock.now();
    }

    /**
     * 구매자가 가격 제안의 응답 기한 연장을 요청한다.
     */
    public void requestExtension() {
        validatePending();
        this.status = NegoOfferStatus.EXTENSION_REQUESTED;
    }

    /**
     * 판매자가 가격 제안 연장 요청을 승인하고 만료 시간을 늘린다.
     */
    public void approveExtension() {
        if (status != NegoOfferStatus.EXTENSION_REQUESTED) {
            throw new IllegalStateException("연장 요청 상태의 가격 제안만 연장 승인할 수 있습니다.");
        }
        this.expiresAt = this.expiresAt.plusHours(12);
        this.status = NegoOfferStatus.EXTENDED;
    }

    /**
     * 판매자가 가격 제안 연장 요청을 거절한다.
     */
    public void rejectExtension() {
        if (status != NegoOfferStatus.EXTENSION_REQUESTED) {
            throw new IllegalStateException("연장 요청 상태의 가격 제안만 연장 거절할 수 있습니다.");
        }
        this.status = NegoOfferStatus.PENDING;
    }

    /**
     * 조건 충족 여부를 확인한다.
     * @param now 현재 시각
     * @return 클라이언트에 반환할 API 응답
     */
    public boolean isExpired(LocalDateTime now) {
        return !expiresAt.isAfter(now);
    }

    /**
     * 도메인 객체를 만료 상태로 변경한다.
     * @param now 현재 시각
     */
    public void expire(LocalDateTime now) {
        if (this.status == NegoOfferStatus.ACCEPTED
            || this.status == NegoOfferStatus.REJECTED
            || this.status == NegoOfferStatus.EXPIRED
            || this.status == NegoOfferStatus.CANCELLED) {
            return;
        }
        if (!isExpired(now)) {
            throw new IllegalStateException("만료 시간이 지나지 않은 가격 제안입니다.");
        }
        this.status = NegoOfferStatus.EXPIRED;
        this.respondedAt = now;
    }

    /**
     * 도메인 객체를 취소 상태로 변경한다.
     */
    public void cancel() {
        validatePending();
        this.status = NegoOfferStatus.CANCELLED;
        this.respondedAt = AgoraClock.now();
    }

    private void validatePending() {
        if (!ACTIVE_STATUSES.contains(status)) {
            throw new IllegalStateException("응답 가능한 가격 제안 상태가 아닙니다.");
        }
    }
}
