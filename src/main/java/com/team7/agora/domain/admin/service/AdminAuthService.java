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
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자 인증 관련 비즈니스 유스케이스를 처리하는 서비스이다.
 */
@Service
@Transactional(readOnly = true)
public class AdminAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param userRepository 데이터를 조회하고 저장하는 리포지토리
     * @param passwordEncoder 비밀번호 해시와 검증에 사용하는 인코더
     * @param jwtProvider JWT 생성과 검증을 담당하는 컴포넌트
     */
    public AdminAuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    /**
     * 로그인 요청의 이메일과 비밀번호를 검증하고 JWT 토큰을 발급한다.
     * @param request 요청 본문
     * @return 클라이언트에 반환할 API 응답
     */
    public AdminLoginResponse login(AdminLoginRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmailIgnoreCase(email)
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
     * 사용자의 리프레시 토큰을 삭제해 로그아웃 상태로 만든다.
     * @param admin 인증된 관리자 정보
     */
    public void logout(CustomUserDetails admin) {
        if (admin == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (!AdminRoleSupport.isAdminRole(admin.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "관리자 권한이 없는 계정입니다.");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
