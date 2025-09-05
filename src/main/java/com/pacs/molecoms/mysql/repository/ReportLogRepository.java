package com.pacs.molecoms.mysql.repository;

import com.pacs.molecoms.mysql.entity.ReportLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportLogRepository extends JpaRepository<ReportLog, Long> {
}
