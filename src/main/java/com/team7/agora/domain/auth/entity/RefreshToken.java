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
 * JPA 엔티티이다.
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
     * 조건 충족 여부를 확인한다.
     * @param user 입력 값
     * @param token 입력 값
     * @param expiresAt 입력 값
     * @return 처리 결과
     */
    public static RefreshToken issue(User user, String token, LocalDateTime expiresAt) {
        return new RefreshToken(user, token, expiresAt);
    }

    /**
     * 조건 충족 여부를 확인한다.
     * @param now 입력 값
     * @return 처리 결과
     */
    public boolean isExpired(LocalDateTime now) {
        return !expiresAt.isAfter(now);
    }
}
