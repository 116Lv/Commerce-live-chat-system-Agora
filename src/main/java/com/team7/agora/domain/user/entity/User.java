// 서비스 사용자 계정을 나타내는 JPA 엔티티
package com.team7.agora.domain.user.entity;

import com.team7.agora.domain.user.enums.UserRole;
import com.team7.agora.domain.user.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User {

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
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    private User(String email, String encodedPassword, String nickname) {
        this.email = email;
        this.password = encodedPassword;
        this.nickname = nickname;
        this.role = UserRole.ROLE_USER;
        this.status = UserStatus.ACTIVE;
    }

    public static User create(String email, String encodedPassword, String nickname) {
        return new User(email, encodedPassword, nickname);
    }

    public void changeStatus(UserStatus status) {
        this.status = status;
    }

    public void changeRole(UserRole role) {
        this.role = role;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
