// 거래 지역 정보를 표현하는 JPA 엔티티
package com.team7.agora.domain.region.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 지역 도메인 정보를 영속화하는 JPA 엔티티이다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "regions",
        indexes = {
                @Index(name = "idx_regions_name", columnList = "name"),
                @Index(name = "idx_regions_sido", columnList = "sido"),
                @Index(name = "idx_regions_sido_sigungu", columnList = "sido, sigungu")
        }
)
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 30)
    private String sido;

    @Column(nullable = false, length = 30)
    private String sigungu;

    @Column(nullable = false, length = 30)
    private String eupmyeondong;

    private Region(String name, String code, String sido, String sigungu, String eupmyeondong) {
        this.name = name;
        this.code = code;
        this.sido = sido;
        this.sigungu = sigungu;
        this.eupmyeondong = eupmyeondong;
    }

    /**
     * 지역 코드와 행정구역 이름으로 거래 지역 엔티티를 생성한다.
     * @param name 이름 또는 제목
     * @param code 지역 코드
     * @param sido 시도 이름
     * @param sigungu 시군구 이름
     * @param eupmyeondong 읍면동 이름
     * @return 클라이언트에 반환할 API 응답
     */
    public static Region create(String name, String code, String sido, String sigungu, String eupmyeondong) {
        return new Region(name, code, sido, sigungu, eupmyeondong);
    }
}
