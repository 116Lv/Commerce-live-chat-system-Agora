// JwtProvider의 토큰 생성·파싱·검증 동작을 검증하는 단위 테스트
package com.team7.agora.global.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;

class JwtProviderTest {

    private static final String SECRET = "localEnvDummySecretKeyForAgoraProjectTest12345!";

    private final JwtProvider jwtProvider = new JwtProvider(SECRET, 3_600_000L);

    @Test
    void createAndParse_returnsClaims() {
        // given
        String bearerToken = jwtProvider.createToken(1L, "user@test.com", "ROLE_USER", "동네유저");

        // when
        JwtClaims claims = jwtProvider.parse(jwtProvider.substringBearer(bearerToken));

        // then
        assertThat(claims.userId()).isEqualTo(1L);
        assertThat(claims.email()).isEqualTo("user@test.com");
        assertThat(claims.role()).isEqualTo("ROLE_USER");
        assertThat(claims.nickname()).isEqualTo("동네유저");
    }

    @Test
    void createAndParse_preservesSpecialCharactersInClaims() {
        // given: nickname에 쉼표/콜론/따옴표/역슬래시 포함
        String nickname = "동네, 유저: \"특수\" \\값";
        String bearerToken = jwtProvider.createToken(1L, "user@test.com", "ROLE_USER", nickname);

        // when
        JwtClaims claims = jwtProvider.parse(jwtProvider.substringBearer(bearerToken));

        // then
        assertThat(claims.nickname()).isEqualTo(nickname);
        assertThat(claims.email()).isEqualTo("user@test.com");
        assertThat(claims.userId()).isEqualTo(1L);
    }

    @Test
    void substringBearer_rejectsMalformedToken() {
        // when & then
        assertThatThrownBy(() -> jwtProvider.substringBearer("wrong-token"))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    void parse_rejectsTamperedSignature() {
        // given
        String token = jwtProvider.substringBearer(
            jwtProvider.createToken(1L, "user@test.com", "ROLE_USER", "동네유저"));
        String tampered = token.substring(0, token.length() - 1)
            + (token.endsWith("A") ? "B" : "A");

        // when & then
        assertThatThrownBy(() -> jwtProvider.parse(tampered))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    void parse_rejectsExpiredToken() {
        // given
        JwtProvider expiredProvider = new JwtProvider(SECRET, -1_000L);
        String token = expiredProvider.substringBearer(
            expiredProvider.createToken(1L, "user@test.com", "ROLE_USER", "동네유저"));

        // when & then
        assertThatThrownBy(() -> expiredProvider.parse(token))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.EXPIRED_TOKEN);
    }
}
