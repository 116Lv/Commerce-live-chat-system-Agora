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
 * JPA 엔티티이다.
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
     * 요청한 동작을 처리한다.
     * @param email 입력 값
     * @param encodedPassword 입력 값
     * @param nickname 입력 값
     * @param phone 입력 값
     * @return 처리 결과
     */
    public static User signup(String email, String encodedPassword, String nickname, String phone) {
        return new User(email, encodedPassword, nickname, phone);
    }

    /**
     * 도메인 객체를 생성한다.
     * @param email 입력 값
     * @param encodedPassword 입력 값
     * @param nickname 입력 값
     * @return 처리 결과
     */
    public static User create(String email, String encodedPassword, String nickname) {
        return new User(email, encodedPassword, nickname, null);
    }

    /**
     * 데이터를 수정한다.
     * @param delta 입력 값
     */
    public void updateSmileScore(int delta) {
        int nextScore = this.smileScore + delta;
        this.smileScore = Math.max(0, Math.min(100, nextScore));
    }

    /**
     * 요청한 동작을 처리한다.
     */
    public void block() {
        this.status = UserStatus.BLOCKED;
    }

    /**
     * 요청한 동작을 처리한다.
     * @param status 입력 값
     */
    public void changeStatus(UserStatus status) {
        this.status = status;
    }

    /**
     * 요청한 동작을 처리한다.
     * @param role 입력 값
     */
    public void changeRole(UserRole role) {
        this.role = role;
    }

    /**
     * 요청한 동작을 처리한다.
     * @param encodedPassword 입력 값
     */
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    /**
     * 데이터를 수정한다.
     * @param nickname 입력 값
     */
    public void updateProfile(String nickname) {
        this.nickname = nickname;
    }
}
