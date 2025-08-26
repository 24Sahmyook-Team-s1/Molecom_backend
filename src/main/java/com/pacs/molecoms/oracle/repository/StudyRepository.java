// com/pacs/molecoms/oracle/repository/OracleStudyRepository.java
package com.pacs.molecoms.oracle.repository;

import com.pacs.molecoms.oracle.entity.StudyEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudyRepository extends JpaRepository<StudyEntity, Long> {

    // pid LIKE %...% AND delFlag = 0
    List<StudyEntity> findByPidContainingIgnoreCaseAndDelFlag(String pidPart, Long delFlag, Sort sort);

    // pid LIKE %...% AND delFlag IS NULL
    List<StudyEntity> findByPidContainingIgnoreCaseAndDelFlagIsNull(String pidPart, Sort sort);

    Optional<StudyEntity> findByStudyInsUid(String studyInsUid);

    List<StudyEntity> findByPid(String pid);

    // pid LIKE %...% AND studydate BETWEEN ... AND modality LIKE ... AND delflag = 0
    List<StudyEntity> findByPidContainingIgnoreCaseAndStudyDateBetweenAndModalityContainingIgnoreCaseAndDelFlag(
            String pidPart, String fromStudyDate, String toStudyDate, String modality, Long delFlag, Sort sort);

    // pid LIKE %...% AND studydate BETWEEN ... AND modality LIKE ... AND delflag IS NULL
    List<StudyEntity> findByPidContainingIgnoreCaseAndStudyDateBetweenAndModalityContainingIgnoreCaseAndDelFlagIsNull(
            String pidPart, String fromStudyDate, String toStudyDate, String modality, Sort sort);



    // 날짜 범위 + modality 필터
    List<StudyEntity> findByPidAndStudyDateBetweenAndModalityContainingIgnoreCase(
            String pid, String studyDate, String studyDate2, String modality);

    // 날짜만 범위 조회
    List<StudyEntity> findByStudyDateBetween(String from, String to);
}
