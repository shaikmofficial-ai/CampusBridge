package com.mgr.campusbridge.repository;

import com.mgr.campusbridge.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByStatusOrderByCreatedAtDesc(Report.ReportStatus status);
    long countByStatus(Report.ReportStatus status);
    List<Report> findByTargetTypeAndTargetId(Report.TargetType targetType, Long targetId);
}
