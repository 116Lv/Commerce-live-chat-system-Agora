package com.team7.agora.domain.admin.service;

import com.team7.agora.domain.admin.dto.response.AdminManagedUserResponse;
import com.team7.agora.domain.admin.enums.AdminPermission;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.auth.AdminPrincipal;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자 회원 관련 비즈니스 유스케이스를 처리하는 서비스이다.
 */
@Service
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param userRepository 데이터를 조회하고 저장하는 리포지토리
     */
    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 'getUsers' 메서드는 필요한 데이터를 조회해 호출한 쪽에 반환한다.
     * @param admin 인증된 관리자 정보
     * @param pageable 페이지 요청 정보
     * @return 클라이언트에 반환할 API 응답
     */
    public Page<AdminManagedUserResponse> getUsers(AdminPrincipal admin, Pageable pageable) {
        validateUserAdmin(admin);
        return userRepository.findAllByDeletedAtIsNull(pageable)
                .map(AdminManagedUserResponse::from);
    }

    /**
     * 'changeStatus' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     * @param admin 인증된 관리자 정보
     * @param userId 회원 ID
     * @param status 조회 또는 변경할 상태
     * @return 클라이언트에 반환할 API 응답
     */
    @Transactional
    public AdminManagedUserResponse changeStatus(AdminPrincipal admin, Long userId, UserStatus status) {
        validateUserAdmin(admin);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        user.changeStatus(status);
        return AdminManagedUserResponse.from(user);
    }

    private void validateUserAdmin(AdminPrincipal admin) {
        if (!AdminRoleSupport.hasPermission(admin, AdminPermission.USER_MANAGE)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "사용자 관리는 관리자만 수행할 수 있습니다.");
        }
    }
}
