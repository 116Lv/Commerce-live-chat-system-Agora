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
 * 회원 도메인 정보를 영속화하는 JPA 엔티티이다.
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
     * 회원가입 요청 정보를 검증하고 새 회원을 등록한다.
     * @param email 이메일
     * @param encodedPassword 암호화된 비밀번호
     * @param nickname 닉네임
     * @param phone 전화번호
     * @return 클라이언트에 반환할 API 응답
     */
    public static User signup(String email, String encodedPassword, String nickname, String phone) {
        return new User(email, encodedPassword, nickname, phone);
    }

    /**
     * 회원가입 요청에서 검증된 이메일과 암호화된 비밀번호로 새 회원 엔티티를 생성한다.
     * @param email 이메일
     * @param encodedPassword 암호화된 비밀번호
     * @param nickname 닉네임
     * @return 클라이언트에 반환할 API 응답
     */
    public static User create(String email, String encodedPassword, String nickname) {
        return new User(email, encodedPassword, nickname, null);
    }

    /**
     * 데이터를 수정한다.
     * @param delta 증감할 점수
     */
    public void updateSmileScore(int delta) {
        int nextScore = this.smileScore + delta;
        this.smileScore = Math.max(0, Math.min(100, nextScore));
    }

    /**
     * 'block' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     */
    public void block() {
        this.status = UserStatus.BLOCKED;
    }

    /**
     * 'changeStatus' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     * @param status 조회 또는 변경할 상태
     */
    public void changeStatus(UserStatus status) {
        this.status = status;
    }

    /**
     * 관리자가 회원 계정의 권한을 변경한다.
     * @param role 권한
     */
    public void changeRole(UserRole role) {
        this.role = role;
    }

    /**
     * 현재 비밀번호를 검증한 뒤 새 비밀번호로 변경한다.
     * @param encodedPassword 암호화된 비밀번호
     */
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    /**
     * 데이터를 수정한다.
     * @param nickname 닉네임
     */
    public void updateProfile(String nickname) {
        this.nickname = nickname;
    }
}
