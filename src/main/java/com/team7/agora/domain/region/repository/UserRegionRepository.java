// 사용자 관심 지역 엔티티에 접근하는 리포지토리
package com.team7.agora.domain.region.repository;

import com.team7.agora.domain.region.entity.UserRegion;
import com.team7.agora.domain.user.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository contract for storing and querying user region data.
 */
public interface UserRegionRepository extends JpaRepository<UserRegion, Long> {

    void deleteByUser(User user);

    List<UserRegion> findAllByUser(User user);
}
