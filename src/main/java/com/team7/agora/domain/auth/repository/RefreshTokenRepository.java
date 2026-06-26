// Refresh Token 영속성 처리를 담당하는 리포지토리
package com.team7.agora.domain.auth.repository;

import com.team7.agora.domain.auth.entity.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 리프레시 토큰 데이터 저장과 조회를 담당하는 저장소 인터페이스이다.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    void deleteByUserId(Long userId);
}
