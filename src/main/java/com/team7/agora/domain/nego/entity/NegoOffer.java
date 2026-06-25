package com.team7.agora.domain.nego.entity;

import com.team7.agora.domain.chat.entity.ChatRoom;
import com.team7.agora.domain.common.entity.BaseTimeEntity;
import com.team7.agora.domain.nego.enums.NegoOfferStatus;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "nego_offers",
    indexes = @Index(name = "idx_nego_offers_chat_room_status", columnList = "chat_room_id, status")
)
public class NegoOffer extends BaseTimeEntity {

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

    public static NegoOffer create(ChatRoom chatRoom, User requester, BigDecimal offerPrice) {
        return new NegoOffer(chatRoom, requester, offerPrice);
    }

    public void accept() {
        validatePending();
        this.status = NegoOfferStatus.ACCEPTED;
        this.respondedAt = LocalDateTime.now();
    }

    public void reject() {
        validatePending();
        this.status = NegoOfferStatus.REJECTED;
        this.respondedAt = LocalDateTime.now();
    }

    public void requestExtension() {
        validatePending();
        this.status = NegoOfferStatus.EXTENSION_REQUESTED;
    }

    public void approveExtension() {
        if (status != NegoOfferStatus.EXTENSION_REQUESTED) {
            throw new IllegalStateException("연장 요청 상태의 가격 제안만 연장 승인할 수 있습니다.");
        }
        this.expiresAt = this.expiresAt.plusHours(12);
        this.status = NegoOfferStatus.EXTENDED;
    }

    public void rejectExtension() {
        if (status != NegoOfferStatus.EXTENSION_REQUESTED) {
            throw new IllegalStateException("연장 요청 상태의 가격 제안만 연장 거절할 수 있습니다.");
        }
        this.status = NegoOfferStatus.PENDING;
    }

    public boolean isExpired(LocalDateTime now) {
        return !expiresAt.isAfter(now);
    }

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

    public void cancel() {
        validatePending();
        this.status = NegoOfferStatus.CANCELLED;
        this.respondedAt = LocalDateTime.now();
    }

    private void validatePending() {
        if (!ACTIVE_STATUSES.contains(status)) {
            throw new IllegalStateException("응답 가능한 가격 제안 상태가 아닙니다.");
        }
    }
}
