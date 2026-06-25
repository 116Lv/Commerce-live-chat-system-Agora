package com.team7.agora.domain.report.repository;

import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.report.entity.Report;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findAllByProductIsNull();

    List<Report> findAllByProductIsNotNull();

    @Query(
            value = "select distinct r.product from Report r where r.product is not null",
            countQuery = "select count(distinct r.product) from Report r where r.product is not null"
    )
    Page<Product> findDistinctReportedProducts(Pageable pageable);
}
