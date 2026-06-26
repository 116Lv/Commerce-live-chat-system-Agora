// 인증/회원가입 관련 비즈니스 로직을 처리하는 서비스
package com.team7.agora.domain.auth.service;

import com.team7.agora.domain.auth.dto.request.LoginRequest;
import com.team7.agora.domain.auth.dto.request.SignupRequest;
import com.team7.agora.domain.auth.dto.response.LoginResponse;
import com.team7.agora.domain.auth.dto.response.ReissueResponse;
import com.team7.agora.domain.auth.dto.response.SignupResponse;
import com.team7.agora.domain.auth.entity.RefreshToken;
import com.team7.agora.domain.auth.repository.RefreshTokenRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.auth.JwtProvider;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 관련 비즈니스 유스케이스를 처리하는 서비스이다.
 */
@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final long refreshTokenValidTime;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param userRepository 데이터를 조회하고 저장하는 리포지토리
     * @param refreshTokenRepository 데이터를 조회하고 저장하는 리포지토리
     * @param passwordEncoder 비밀번호 해시와 검증에 사용하는 인코더
     * @param jwtProvider JWT 생성과 검증을 담당하는 컴포넌트
     * @param refreshTokenValidTime 리프레시 토큰 유효 시간
     */
    public AuthService(
        UserRepository userRepository,
        RefreshTokenRepository refreshTokenRepository,
        PasswordEncoder passwordEncoder,
        JwtProvider jwtProvider,
        @Value("${jwt.refresh-token-valid-time}") long refreshTokenValidTime
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.refreshTokenValidTime = refreshTokenValidTime;
    }

    /**
     * 회원가입 요청 정보를 검증하고 새 회원을 등록한다.
     * @param request 요청 본문
     * @return 클라이언트에 반환할 API 응답
     */
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        User user = User.create(
                email,
                passwordEncoder.encode(request.password()),
                request.nickname()
        );
        User savedUser = saveUserOrThrowDuplicateEmail(user);
        return new SignupResponse(savedUser.getEmail(), savedUser.getNickname());
    }

    private User saveUserOrThrowDuplicateEmail(User user) {
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        validateActiveUser(user);

        String accessToken = createAccessToken(user);
        String refreshToken = issueRefreshToken(user);
        return new LoginResponse(accessToken, refreshToken);
    }

    /**
     * 사용자의 리프레시 토큰을 삭제해 로그아웃 상태로 만든다.
     * @param userId 회원 ID
     */
    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    /**
     * 리프레시 토큰을 검증하고 새 액세스 토큰과 리프레시 토큰을 발급한다.
     * @param refreshTokenValue 재발급에 사용할 리프레시 토큰 문자열
     * @return 클라이언트에 반환할 API 응답
     */
    @Transactional
    public ReissueResponse reissue(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hashRefreshToken(refreshTokenValue))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        if (refreshToken.isExpired(nowUtc())) {
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        }

        User user = refreshToken.getUser();
        if (user.getStatus() != UserStatus.ACTIVE) {
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessException(ErrorCode.INACTIVE_USER);
        }
        refreshTokenRepository.delete(refreshToken);

        String accessToken = createAccessToken(user);
        String newRefreshToken = issueRefreshToken(user);
        return new ReissueResponse(accessToken, newRefreshToken);
    }

    private String createAccessToken(User user) {
        return jwtProvider.createToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getNickname()
        );
    }

    private String issueRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = nowUtc().plus(Duration.ofMillis(refreshTokenValidTime));
        refreshTokenRepository.save(RefreshToken.issue(user, hashRefreshToken(token), expiresAt));
        return token;
    }

    public static String hashRefreshToken(String refreshToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(refreshToken.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to hash refresh token.", e);
        }
    }

    private void validateActiveUser(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.INACTIVE_USER);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
}
