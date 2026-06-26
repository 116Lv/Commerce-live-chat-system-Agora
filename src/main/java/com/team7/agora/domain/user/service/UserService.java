package com.team7.agora.domain.user.service;

import com.team7.agora.domain.user.dto.response.SmileScoreResponse;
import com.team7.agora.domain.user.dto.response.UserMeResponse;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 애플리케이션 유스케이스를 조정하는 서비스이다.
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param userRepository 입력 값
     * @param passwordEncoder 입력 값
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 데이터를 반환한다.
     * @param userId 입력 값
     * @return 처리 결과
     */
    public UserMeResponse getMe(Long userId) {
        return UserMeResponse.from(getUser(userId));
    }

    /**
     * 데이터를 반환한다.
     * @param userId 입력 값
     * @return 처리 결과
     */
    public SmileScoreResponse getSmileScore(Long userId) {
        return SmileScoreResponse.from(getUser(userId));
    }

    /**
     * 데이터를 수정한다.
     * @param userId 입력 값
     * @param nickname 입력 값
     * @return 처리 결과
     */
    @Transactional
    public UserMeResponse updateProfile(Long userId, String nickname) {
        User user = getUser(userId);
        user.updateProfile(nickname);
        return UserMeResponse.from(user);
    }

    /**
     * 요청한 동작을 처리한다.
     * @param userId 입력 값
     * @param currentPassword 입력 값
     * @param newPassword 입력 값
     */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = getUser(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "현재 비밀번호가 일치하지 않습니다.");
        }
        user.changePassword(passwordEncoder.encode(newPassword));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다."));
    }
}
