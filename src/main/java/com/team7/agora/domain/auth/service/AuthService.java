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
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service that coordinates auth use cases.
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
     * Creates a auth service instance.
     * @param userRepository the user repository value
     * @param refreshTokenRepository the refresh token repository value
     * @param passwordEncoder the password encoder value
     * @param jwtProvider the jwt provider value
     * @param refreshTokenValidTime the refresh token valid time value
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
     * Handles signup behavior.
     * @param request the request value
     * @return the signup result
     */
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        User user = User.create(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.nickname()
        );
        User savedUser = userRepository.save(user);
        return new SignupResponse(savedUser.getEmail(), savedUser.getNickname());
    }

    /**
     * Handles login behavior.
     * @param request the request value
     * @return the login result
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
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
     * Handles logout behavior.
     * @param userId the user id value
     */
    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    /**
     * Handles reissue behavior.
     * @param refreshTokenValue the refresh token value value
     * @return the reissue result
     */
    @Transactional
    public ReissueResponse reissue(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
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
        refreshTokenRepository.save(RefreshToken.issue(user, token, expiresAt));
        return token;
    }

    private void validateActiveUser(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.INACTIVE_USER);
        }
    }

    private LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
}
