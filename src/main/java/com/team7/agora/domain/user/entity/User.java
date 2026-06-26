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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA entity that represents an user record.
 */
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

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    private int smileScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    private LocalDateTime deletedAt;

    private User(String email, String password, String nickname, String phone) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.phone = phone;
        this.smileScore = 60;
        this.role = UserRole.ROLE_USER;
        this.status = UserStatus.ACTIVE;
    }

    /**
     * Handles signup behavior.
     * @param email the email value
     * @param encodedPassword the encoded password value
     * @param nickname the nickname value
     * @param phone the phone value
     * @return the signup result
     */
    public static User signup(String email, String encodedPassword, String nickname, String phone) {
        return new User(email, encodedPassword, nickname, phone);
    }

    /**
     * Creates create data.
     * @param email the email value
     * @param encodedPassword the encoded password value
     * @param nickname the nickname value
     * @return the create result
     */
    public static User create(String email, String encodedPassword, String nickname) {
        return new User(email, encodedPassword, nickname, null);
    }

    /**
     * Updates smile score data.
     * @param delta the delta value
     */
    public void updateSmileScore(int delta) {
        int nextScore = this.smileScore + delta;
        this.smileScore = Math.max(0, Math.min(100, nextScore));
    }

    /**
     * Handles block behavior.
     */
    public void block() {
        this.status = UserStatus.BLOCKED;
    }

    /**
     * Handles change status behavior.
     * @param status the status value
     */
    public void changeStatus(UserStatus status) {
        this.status = status;
    }

    /**
     * Handles change role behavior.
     * @param role the role value
     */
    public void changeRole(UserRole role) {
        this.role = role;
    }

    /**
     * Handles change password behavior.
     * @param encodedPassword the encoded password value
     */
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    /**
     * Updates profile data.
     * @param nickname the nickname value
     */
    public void updateProfile(String nickname) {
        this.nickname = nickname;
    }
}
