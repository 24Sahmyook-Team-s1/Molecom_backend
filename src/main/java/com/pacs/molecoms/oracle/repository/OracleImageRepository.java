package com.pacs.molecoms.oracle.repository;

import com.pacs.molecoms.oracle.entity.OracleImage;
import com.pacs.molecoms.oracle.entity.OracleImageId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OracleImageRepository extends JpaRepository<OracleImage, OracleImageId> {
    Optional<OracleImage> findByStudyKeyAndSeriesKeyAndImageKey(Long studyKey, Long seriesKey, Long imageKey);
}
