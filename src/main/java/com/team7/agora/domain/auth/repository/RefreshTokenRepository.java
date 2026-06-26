// Refresh Token 영속성 처리를 담당하는 리포지토리
package com.team7.agora.domain.auth.repository;

import com.team7.agora.domain.auth.entity.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository contract for storing and querying refresh token data.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUserId(Long userId);
}
