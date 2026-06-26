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
 * Authentication component for custom user behavior.
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
     * Creates a custom user details instance.
     * @param userId the user id value
     * @param email the email value
     * @param password the password value
     * @param role the role value
     * @param status the status value
     * @param nickname the nickname value
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
     * Creates a response from the given domain object.
     * @param user the user value
     * @return the from result
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
     * Returns authorities data.
     * @return the get authorities result
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    /**
     * Returns password data.
     * @return the get password result
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns username data.
     * @return the get username result
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Checks whether is account non expired applies.
     * @return the is account non expired result
     */
    @Override
    public boolean isAccountNonExpired() {
        return status != UserStatus.DELETED;
    }

    /**
     * Checks whether is account non locked applies.
     * @return the is account non locked result
     */
    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.SUSPENDED && status != UserStatus.BLOCKED;
    }

    /**
     * Checks whether is credentials non expired applies.
     * @return the is credentials non expired result
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Checks whether is enabled applies.
     * @return the is enabled result
     */
    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
}
