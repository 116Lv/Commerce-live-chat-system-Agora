package com.team7.agora.domain.review.entity;

import com.team7.agora.domain.common.entity.BaseTimeEntity;
import com.team7.agora.domain.trade.entity.Trade;
import com.team7.agora.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 리뷰 도메인 정보를 영속화하는 JPA 엔티티이다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "reviews",
    uniqueConstraints = @UniqueConstraint(columnNames = {"trade_id", "reviewer_id"})
)
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id", nullable = false)
    private Trade trade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable = false)
    private User targetUser;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false, length = 500)
    private String content;

    private Review(Trade trade, User reviewer, User targetUser, int rating, String content) {
        this.trade = trade;
        this.reviewer = reviewer;
        this.targetUser = targetUser;
        this.rating = rating;
        this.content = content;
        markCreatedNow();
    }

    /**
     * 거래와 작성자, 대상 회원, 평점을 기준으로 새 후기 엔티티를 생성한다.
     * @param trade 거래 엔티티 또는 거래 응답 변환 대상
     * @param reviewer 후기를 작성한 회원 엔티티
     * @param targetUser 후기를 받는 회원 엔티티
     * @param rating 후기 평점
     * @param content 내용
     * @return 클라이언트에 반환할 API 응답
     */
    public static Review create(Trade trade, User reviewer, User targetUser, int rating, String content) {
        return new Review(trade, reviewer, targetUser, rating, content);
    }

    private static int toSmileDelta(int rating) {
        return switch (rating) {
            case 1 -> -4;
            case 2 -> -2;
            case 3 -> 0;
            case 4 -> 2;
            case 5 -> 4;
            default -> throw new IllegalArgumentException("평점은 1점부터 5점까지 입력할 수 있습니다.");
        };
    }
}
