package com.team7.agora.domain.admin.repository;

import com.team7.agora.domain.admin.entity.Admin;
import com.team7.agora.domain.admin.enums.AdminRole;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByEmailIgnoreCase(String email);

    long countByRole(AdminRole role);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Admin a where a.role = :role")
    List<Admin> findAllByRoleForUpdate(@Param("role") AdminRole role);
}
