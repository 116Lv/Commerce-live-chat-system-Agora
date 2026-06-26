// 로그인 유지/토큰 재발급에 사용되는 Refresh Token 엔티티
package com.team7.agora.domain.auth.entity;

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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA entity that represents a refresh token record.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 100)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private RefreshToken(User user, String token, LocalDateTime expiresAt) {
        this.user = user;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    /**
     * Checks whether issue applies.
     * @param user the user value
     * @param token the token value
     * @param expiresAt the expires at value
     * @return the issue result
     */
    public static RefreshToken issue(User user, String token, LocalDateTime expiresAt) {
        return new RefreshToken(user, token, expiresAt);
    }

    /**
     * Checks whether is expired applies.
     * @param now the now value
     * @return the is expired result
     */
    public boolean isExpired(LocalDateTime now) {
        return !expiresAt.isAfter(now);
    }
}
