// 지역 엔티티에 접근하는 리포지토리
package com.team7.agora.domain.region.repository;

import com.team7.agora.domain.region.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Long> {
}
