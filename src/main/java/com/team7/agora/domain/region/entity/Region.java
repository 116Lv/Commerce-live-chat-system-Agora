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
 * JPA entity that represents a region record.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "regions",
        indexes = @Index(name = "idx_regions_name", columnList = "name")
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
     * Creates create data.
     * @param name the name value
     * @param code the code value
     * @param sido the sido value
     * @param sigungu the sigungu value
     * @param eupmyeondong the eupmyeondong value
     * @return the create result
     */
    public static Region create(String name, String code, String sido, String sigungu, String eupmyeondong) {
        return new Region(name, code, sido, sigungu, eupmyeondong);
    }
}
