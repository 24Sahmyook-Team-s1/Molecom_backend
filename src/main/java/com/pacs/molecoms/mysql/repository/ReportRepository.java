package com.pacs.molecoms.mysql.repository;

import com.pacs.molecoms.mysql.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByStudyKey(Long studyKey);
    List<Report> findByStudyKeyAndSeriesKey(Long studyKey, Long seriesKey);
}
