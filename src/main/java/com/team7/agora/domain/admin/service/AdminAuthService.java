// 관리자 로그인과 로그아웃을 처리하는 서비스
package com.team7.agora.domain.admin.service;

import com.team7.agora.domain.admin.dto.request.AdminLoginRequest;
import com.team7.agora.domain.admin.dto.response.AdminLoginResponse;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.auth.CustomUserDetails;
import com.team7.agora.global.auth.JwtProvider;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service that coordinates admin auth use cases.
 */
@Service
@Transactional(readOnly = true)
public class AdminAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    /**
     * Creates a admin auth service instance.
     * @param userRepository the user repository value
     * @param passwordEncoder the password encoder value
     * @param jwtProvider the jwt provider value
     */
    public AdminAuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    /**
     * Handles login behavior.
     * @param request the request value
     * @return the login result
     */
    public AdminLoginResponse login(AdminLoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.INACTIVE_USER);
        }
        if (!AdminRoleSupport.isAdminRole(user.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "관리자 권한이 없는 계정입니다.");
        }

        String accessToken = jwtProvider.createToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getNickname()
        );
        return new AdminLoginResponse(accessToken);
    }

    /**
     * Handles logout behavior.
     * @param admin the admin value
     */
    public void logout(CustomUserDetails admin) {
        if (admin == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (!AdminRoleSupport.isAdminRole(admin.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "관리자 권한이 없는 계정입니다.");
        }
    }
}
