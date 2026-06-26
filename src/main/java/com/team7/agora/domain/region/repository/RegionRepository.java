package com.team7.agora.domain.region.repository;

import com.team7.agora.domain.region.entity.Region;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository contract for storing and querying region data.
 */
public interface RegionRepository extends JpaRepository<Region, Long> {

    List<Region> findByNameContaining(String keyword);

    boolean existsByCode(String code);
}
