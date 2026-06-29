package com.team7.agora.domain.report.repository;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.report.entity.Report;
import com.team7.agora.domain.report.enums.ReportStatus;
import com.team7.agora.domain.user.entity.User;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 신고 데이터 저장과 조회를 담당하는 저장소 인터페이스이다.
 */
public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByReporterAndProductAndStatus(User reporter, Product product, ReportStatus status);

    boolean existsByReporterAndReportedUserAndProductIsNullAndStatus(
        User reporter,
        User reportedUser,
        ReportStatus status
    );

    @EntityGraph(attributePaths = {"reporter", "reportedUser"})
    List<Report> findAllByProductIsNull();

    @EntityGraph(attributePaths = {"reporter", "reportedUser", "product"})
    List<Report> findAllByProductIsNotNull();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Report r where r.id = :id")
    Optional<Report> findByIdForUpdate(@Param("id") Long id);

    @Query(
            value = "select distinct r.product from Report r where r.product is not null",
            countQuery = "select count(distinct r.product) from Report r where r.product is not null"
    )
    Page<Product> findDistinctReportedProducts(Pageable pageable);
}
