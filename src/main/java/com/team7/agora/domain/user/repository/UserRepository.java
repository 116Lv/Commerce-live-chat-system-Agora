// User 엔티티에 대한 영속성 접근을 담당하는 리포지토리
package com.team7.agora.domain.user.repository;

import com.team7.agora.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
