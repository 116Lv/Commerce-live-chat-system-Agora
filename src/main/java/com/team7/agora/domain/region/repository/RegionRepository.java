package com.team7.agora.domain.region.repository;

import com.team7.agora.domain.region.entity.Region;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 지역 데이터 저장과 조회를 담당하는 저장소 인터페이스이다.
 */
public interface RegionRepository extends JpaRepository<Region, Long> {

    List<Region> findByNameContaining(String keyword);

    boolean existsByCode(String code);

    @Query("SELECT DISTINCT r.sido FROM Region r ORDER BY r.sido ASC")
    List<String> findDistinctSidoOrderBySido();

    @Query("SELECT DISTINCT r.sigungu FROM Region r WHERE r.sido = :sido ORDER BY r.sigungu ASC")
    List<String> findDistinctSigunguBySidoOrderBySigungu(@Param("sido") String sido);

    List<Region> findBySidoAndSigunguOrderByEupmyeondongAsc(String sido, String sigungu);
}
