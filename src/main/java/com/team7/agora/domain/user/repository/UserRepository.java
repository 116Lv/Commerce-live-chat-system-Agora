package com.team7.agora.domain.user.repository;

import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 회원 데이터 저장과 조회를 담당하는 저장소 인터페이스이다.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    Optional<User> findByIdAndStatusAndDeletedAtIsNull(Long id, UserStatus status);

    List<User> findAllByNicknameAndStatusAndDeletedAtIsNull(String nickname, UserStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);

    List<User> findBySmileScoreGreaterThanEqualAndStatus(int smileScore, UserStatus status);

    List<User> findAllByStatus(UserStatus status);

    Page<User> findAllByStatus(UserStatus status, Pageable pageable);

    Page<User> findAllByDeletedAtIsNull(Pageable pageable);

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime start, LocalDateTime end);
}
