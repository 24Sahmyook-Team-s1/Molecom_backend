// com/pacs/molecoms/oracle/repo/SeriesRepository.java
package com.pacs.molecoms.oracle.repository;

import com.pacs.molecoms.oracle.entity.SeriesEntity;
import com.pacs.molecoms.oracle.entity.id.SeriesId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeriesRepository extends JpaRepository<SeriesEntity, SeriesId> {
    Optional<SeriesEntity> findBySeriesInsUid(String seriesInsUid);
    List<SeriesEntity> findByStudyKeyOrderBySeriesNumAsc(Long studyKey);
    Optional<SeriesEntity> findByStudyKeyAndSeriesKey(Long studyKey, Long seriesKey);
    Optional<SeriesEntity> findByStudyInsUid(String studyInsUid);
}
