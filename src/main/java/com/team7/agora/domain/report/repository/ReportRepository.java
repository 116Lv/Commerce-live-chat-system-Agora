package com.team7.agora.domain.report.repository;

import com.team7.agora.domain.report.entity.Report;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findAllByProductIsNull();

    List<Report> findAllByProductIsNotNull();
}
