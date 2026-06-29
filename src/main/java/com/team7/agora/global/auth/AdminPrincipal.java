package com.team7.agora.global.auth;

import com.team7.agora.domain.admin.entity.Admin;
import com.team7.agora.domain.admin.enums.AdminRole;
import com.team7.agora.domain.admin.enums.AdminStatus;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class AdminPrincipal implements UserDetails {

    private final Long adminId;
    private final String email;
    private final String password;
    private final AdminRole role;
    private final AdminStatus status;
    private final String nickname;

    public AdminPrincipal(
        Long adminId,
        String email,
        String password,
        AdminRole role,
        AdminStatus status,
        String nickname
    ) {
        this.adminId = adminId;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
        this.nickname = nickname;
    }

    public static AdminPrincipal from(Admin admin) {
        return new AdminPrincipal(
            admin.getId(),
            admin.getEmail(),
            admin.getPassword(),
            admin.getRole(),
            admin.getStatus(),
            admin.getNickname()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return status != AdminStatus.DELETED;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != AdminStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == AdminStatus.ACTIVE;
    }
}
