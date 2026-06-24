package com.team7.agora.domain.region.repository;

import com.team7.agora.domain.region.entity.UserRegion;
import com.team7.agora.domain.user.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRegionRepository extends JpaRepository<UserRegion, Long> {

    void deleteByUser(User user);

    List<UserRegion> findAllByUser(User user);
}
