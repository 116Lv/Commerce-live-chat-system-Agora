package com.team7.agora.domain.user.repository;

import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    List<User> findBySmileScoreGreaterThanEqualAndStatus(int smileScore, UserStatus status);

    List<User> findAllByStatus(UserStatus status);
}
