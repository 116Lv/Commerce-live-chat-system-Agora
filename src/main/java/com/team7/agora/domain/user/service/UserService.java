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
 * Application service that coordinates user use cases.
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a user service instance.
     * @param userRepository the user repository value
     * @param passwordEncoder the password encoder value
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Returns me data.
     * @param userId the user id value
     * @return the get me result
     */
    public UserMeResponse getMe(Long userId) {
        return UserMeResponse.from(getUser(userId));
    }

    /**
     * Returns smile score data.
     * @param userId the user id value
     * @return the get smile score result
     */
    public SmileScoreResponse getSmileScore(Long userId) {
        return SmileScoreResponse.from(getUser(userId));
    }

    /**
     * Updates profile data.
     * @param userId the user id value
     * @param nickname the nickname value
     * @return the update profile result
     */
    @Transactional
    public UserMeResponse updateProfile(Long userId, String nickname) {
        User user = getUser(userId);
        user.updateProfile(nickname);
        return UserMeResponse.from(user);
    }

    /**
     * Handles change password behavior.
     * @param userId the user id value
     * @param currentPassword the current password value
     * @param newPassword the new password value
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
