package com.team7.agora.domain.admin.entity;

import com.team7.agora.domain.admin.enums.AdminRole;
import com.team7.agora.domain.admin.enums.AdminPermission;
import com.team7.agora.domain.admin.enums.AdminStatus;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "admins")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AdminRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdminStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "admin_permissions", joinColumns = @JoinColumn(name = "admin_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false, length = 40)
    private Set<AdminPermission> permissions = new LinkedHashSet<>();

    private Admin(String email, String password, String nickname, AdminRole role) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
        this.status = AdminStatus.ACTIVE;
    }

    public static Admin create(String email, String encodedPassword, String nickname, AdminRole role) {
        return new Admin(email, encodedPassword, nickname, role);
    }

    public void changeRole(AdminRole role) {
        if (this.role == role) {
            throw new BusinessException(ErrorCode.CONFLICT, "Already assigned this admin role.");
        }
        this.role = role;
    }

    public void grantPermission(AdminPermission permission) {
        permissions.add(permission);
    }

    public Set<AdminPermission> getEffectivePermissions() {
        Set<AdminPermission> effectivePermissions = new LinkedHashSet<>(AdminPermission.defaultsFor(role));
        effectivePermissions.addAll(permissions);
        return effectivePermissions;
    }
}
