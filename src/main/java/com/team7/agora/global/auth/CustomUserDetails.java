// 인증된 사용자 정보를 담는 Spring Security UserDetails 구현체
package com.team7.agora.global.auth;

import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.enums.UserStatus;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 인증 처리를 담당하는 컴포넌트이다.
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String email;
    private final String password;
    private final UserRole role;
    private final UserStatus status;
    private final String nickname;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param userId 입력 값
     * @param email 입력 값
     * @param password 입력 값
     * @param role 입력 값
     * @param status 입력 값
     * @param nickname 입력 값
     */
    public CustomUserDetails(
        Long userId,
        String email,
        String password,
        UserRole role,
        UserStatus status,
        String nickname
    ) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
        this.nickname = nickname;
    }

    /**
     * 도메인 객체로부터 응답 객체를 생성한다.
     * @param user 입력 값
     * @return 처리 결과
     */
    public static CustomUserDetails from(User user) {
        return new CustomUserDetails(
            user.getId(),
            user.getEmail(),
            user.getPassword(),
            user.getRole(),
            user.getStatus(),
            user.getNickname()
        );
    }

    /**
     * 데이터를 반환한다.
     * @return 처리 결과
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    /**
     * 데이터를 반환한다.
     * @return 처리 결과
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * 데이터를 반환한다.
     * @return 처리 결과
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * 조건 충족 여부를 확인한다.
     * @return 처리 결과
     */
    @Override
    public boolean isAccountNonExpired() {
        return status != UserStatus.DELETED;
    }

    /**
     * 조건 충족 여부를 확인한다.
     * @return 처리 결과
     */
    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.SUSPENDED && status != UserStatus.BLOCKED;
    }

    /**
     * 조건 충족 여부를 확인한다.
     * @return 처리 결과
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 조건 충족 여부를 확인한다.
     * @return 처리 결과
     */
    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }

    public AuthUser toAuthUser() {
        return new AuthUser(userId, email, role.name(), nickname);
    }
}
