package com.team7.agora.domain.region.repository;

import com.team7.agora.domain.region.entity.Region;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 지역 데이터 저장과 조회를 담당하는 저장소 인터페이스이다.
 */
public interface RegionRepository extends JpaRepository<Region, Long> {

    List<Region> findByNameContaining(String keyword);

    boolean existsByCode(String code);
}
