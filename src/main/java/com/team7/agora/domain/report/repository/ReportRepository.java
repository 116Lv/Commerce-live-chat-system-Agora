package com.team7.agora.domain.report.repository;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.report.entity.Report;
import com.team7.agora.domain.user.entity.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * 신고 데이터 저장과 조회를 담당하는 저장소 인터페이스이다.
 */
public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByReporterAndProduct(User reporter, Product product);

    boolean existsByReporterAndReportedUserAndProductIsNull(User reporter, User reportedUser);

    List<Report> findAllByProductIsNull();

    List<Report> findAllByProductIsNotNull();

    @Query(
            value = "select distinct r.product from Report r where r.product is not null",
            countQuery = "select count(distinct r.product) from Report r where r.product is not null"
    )
    Page<Product> findDistinctReportedProducts(Pageable pageable);
}
