package com.pacs.molecoms.oracle.repository;

import com.pacs.molecoms.oracle.entity.ImageEntity;
import com.pacs.molecoms.oracle.entity.id.ImageId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<ImageEntity, ImageId> {
    Optional<ImageEntity> findByStudyKeyAndSeriesKeyAndImageKey(Long studyKey, Long seriesKey, Long imageKey);
    List<ImageEntity> findByStudyKeyAndSeriesKeyOrderByImageKeyAsc(Long studyKey, Long seriesKey);
    List<ImageEntity> findByStudyKeyAndSeriesKey(Long studyKey, Long seriesKey);
    List<ImageEntity> findBySopInstanceUid(String sopInstanceUid);
    List<ImageEntity> findByStudyKeyAndSeriesKeyIn(Long studyKey, Collection<Long> seriesKeys);
}
