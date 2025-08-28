package com.pacs.molecoms.mysql.repository;

import com.pacs.molecoms.mysql.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    // ✅ Study 단위 조회
    List<Report> findByStudyKey(Long studyKey);

    // ✅ Study + Series 단위 조회
    List<Report> findByStudyKeyAndSeriesKey(Long studyKey, Long seriesKey);
}
