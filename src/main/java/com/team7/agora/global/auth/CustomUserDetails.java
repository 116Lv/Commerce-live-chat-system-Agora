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
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param userId 회원 ID
     * @param email 이메일
     * @param password 비밀번호
     * @param role 권한
     * @param status 조회 또는 변경할 상태
     * @param nickname 닉네임
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
     * @param user 회원 엔티티
     * @return 클라이언트에 반환할 API 응답
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
     * 'getAuthorities' 메서드는 필요한 데이터를 조회해 호출한 쪽에 반환한다.
     * @return 클라이언트에 반환할 API 응답
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    /**
     * 'getPassword' 메서드는 필요한 데이터를 조회해 호출한 쪽에 반환한다.
     * @return 클라이언트에 반환할 API 응답
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * 'getUsername' 메서드는 필요한 데이터를 조회해 호출한 쪽에 반환한다.
     * @return 클라이언트에 반환할 API 응답
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * 조건 충족 여부를 확인한다.
     * @return 클라이언트에 반환할 API 응답
     */
    @Override
    public boolean isAccountNonExpired() {
        return status != UserStatus.DELETED;
    }

    /**
     * 조건 충족 여부를 확인한다.
     * @return 클라이언트에 반환할 API 응답
     */
    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.SUSPENDED && status != UserStatus.BLOCKED;
    }

    /**
     * 조건 충족 여부를 확인한다.
     * @return 클라이언트에 반환할 API 응답
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 조건 충족 여부를 확인한다.
     * @return 클라이언트에 반환할 API 응답
     */
    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }

    public AuthUser toAuthUser() {
        return new AuthUser(userId, email, role.name(), nickname);
    }
}
