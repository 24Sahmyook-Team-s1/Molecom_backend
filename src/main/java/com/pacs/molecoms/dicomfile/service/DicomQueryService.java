// com/pacs/molecoms/oracle/service/DicomQueryService.java
package com.pacs.molecoms.dicomfile.service;

import com.pacs.molecoms.dicomfile.dto.StudySummaryDto;
import com.pacs.molecoms.dicomfile.dto.SeriesDto;
import com.pacs.molecoms.dicomfile.dto.ImageDto;
import com.pacs.molecoms.oracle.entity.StudyEntity;
import com.pacs.molecoms.oracle.entity.SeriesEntity;
import com.pacs.molecoms.oracle.entity.ImageEntity;
import com.pacs.molecoms.oracle.repository.StudyRepository;
import com.pacs.molecoms.oracle.repository.SeriesRepository;
import com.pacs.molecoms.oracle.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DicomQueryService {

    private final StudyRepository studyRepository;
    private final SeriesRepository seriesRepository;
    private final ImageRepository imageRepository;

    @Transactional(readOnly = true, transactionManager = "oracleTx")
    public List<StudySummaryDto> searchStudies(String patientId,
                                               LocalDate from,
                                               LocalDate to,
                                               String modality) {
        Sort sort = Sort.by(Sort.Order.desc("studyDate"), Sort.Order.desc("studyTime"));

        String fromStr = (from != null) ? from.format(DateTimeFormatter.ofPattern("yyyyMMdd")) : "00000000";
        String toStr   = (to != null) ? to.format(DateTimeFormatter.ofPattern("yyyyMMdd"))   : "99991231";
        String mod = (modality == null) ? "" : modality.trim();

        List<StudyEntity> valid = studyRepository
                .findByPidContainingIgnoreCaseAndStudyDateBetweenAndModalityContainingIgnoreCaseAndDelFlag(
                        patientId, fromStr, toStr, mod, 0L, sort);

        List<StudyEntity> nulls = studyRepository
                .findByPidContainingIgnoreCaseAndStudyDateBetweenAndModalityContainingIgnoreCaseAndDelFlagIsNull(
                        patientId, fromStr, toStr, mod, sort);

        // Map으로 중복 제거 후 values() -> List
        List<StudyEntity> merged = Stream.concat(valid.stream(), nulls.stream())
                .collect(Collectors.toMap(
                        StudyEntity::getStudyKey,
                        e -> e,
                        (e1, e2) -> e1, // 같은 키 있으면 첫 번째 유지
                        LinkedHashMap::new
                ))
                .values().stream().toList();

        return merged.stream().map(StudySummaryDto::from).toList();
    }

    @Transactional(readOnly = true, transactionManager = "oracleTx")
    public List<SeriesDto> getSeriesByStudyUid(String studyInsUid) {
        StudyEntity study = studyRepository.findByStudyInsUid(studyInsUid)
                .orElseThrow(() -> new RuntimeException("Study not found: " + studyInsUid));

        List<SeriesEntity> seriesList = seriesRepository.findByStudyKeyOrderBySeriesNumAsc(study.getStudyKey());

        return seriesList.stream()
                .map(SeriesDto::from)
                .toList();
    }

    @Transactional(readOnly = true, transactionManager = "oracleTx")
    public List<ImageDto> listImagesBySeriesUid(String seriesInsUid) {
        // 1) 시리즈 조회
        SeriesEntity series = seriesRepository.findBySeriesInsUid(seriesInsUid)
                .orElseThrow(() -> new RuntimeException("Series not found: " + seriesInsUid));

        // 2) 해당 시리즈에 속한 이미지들 조회 (정렬 필요 시 OrderBy)
        List<ImageEntity> images = imageRepository.findByStudyKeyAndSeriesKeyOrderByImageKeyAsc(
                series.getStudyKey(),
                series.getSeriesKey()
        );

        // 3) DTO 변환
        return images.stream()
                .map(ImageDto::from)
                .toList();
    }



    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private String toStudyDateString(LocalDate date) {
        return (date == null) ? null : date.format(FMT);
    }

}
