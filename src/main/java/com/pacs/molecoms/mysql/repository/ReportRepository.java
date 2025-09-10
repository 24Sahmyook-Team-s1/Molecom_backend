package com.pacs.molecoms.mysql.repository;

import com.pacs.molecoms.mysql.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Optional<Report> findByStudyKey(Long studyKey);
    boolean existsByStudyKey(Long studyKey);
}

