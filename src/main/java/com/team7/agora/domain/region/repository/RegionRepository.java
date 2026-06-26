package com.team7.agora.domain.region.repository;

import com.team7.agora.domain.region.entity.Region;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 데이터 저장과 조회를 위한 저장소 계약이다.
 */
public interface RegionRepository extends JpaRepository<Region, Long> {

    List<Region> findByNameContaining(String keyword);

    boolean existsByCode(String code);
}
