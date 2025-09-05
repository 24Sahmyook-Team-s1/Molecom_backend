package com.pacs.molecoms.mysql.repository;

import com.pacs.molecoms.mysql.entity.DicomLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DicomLogRepository extends JpaRepository<DicomLog, Long> {
}