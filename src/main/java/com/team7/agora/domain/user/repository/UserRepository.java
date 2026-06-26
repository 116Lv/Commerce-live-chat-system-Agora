package com.team7.agora.domain.user.repository;

import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 회원 데이터 저장과 조회를 담당하는 저장소 인터페이스이다.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    List<User> findBySmileScoreGreaterThanEqualAndStatus(int smileScore, UserStatus status);

    List<User> findAllByStatus(UserStatus status);

    Page<User> findAllByStatus(UserStatus status, Pageable pageable);

    Page<User> findAllByDeletedAtIsNull(Pageable pageable);
}
