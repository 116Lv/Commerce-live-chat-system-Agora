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
 * JPA 엔티티이다.
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
     * 도메인 객체를 생성한다.
     * @param trade 입력 값
     * @param reviewer 입력 값
     * @param targetUser 입력 값
     * @param rating 입력 값
     * @param content 입력 값
     * @return 처리 결과
     */
    public static Review create(Trade trade, User reviewer, User targetUser, int rating, String content) {
        targetUser.updateSmileScore(toSmileDelta(rating));
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
