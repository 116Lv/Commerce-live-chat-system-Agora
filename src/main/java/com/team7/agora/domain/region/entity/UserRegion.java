// 사용자가 선택한 관심 지역을 표현하는 JPA 엔티티
package com.team7.agora.domain.region.entity;

import com.team7.agora.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 선호 지역 도메인 정보를 영속화하는 JPA 엔티티이다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user_regions")
public class UserRegion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(nullable = false)
    private boolean primaryRegion;

    private UserRegion(User user, Region region, boolean primaryRegion) {
        this.user = user;
        this.region = region;
        this.primaryRegion = primaryRegion;
    }

    /**
     * 도메인 객체를 클라이언트 응답 DTO로 변환한다.
     * @param user 회원 엔티티
     * @param region 거래 지역 엔티티
     * @param primaryRegion 대표 거래 지역 여부
     * @return 클라이언트에 반환할 API 응답
     */
    public static UserRegion of(User user, Region region, boolean primaryRegion) {
        return new UserRegion(user, region, primaryRegion);
    }
}
