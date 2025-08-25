// com/pacs/molecoms/oracle/repository/OracleStudyRepository.java
package com.pacs.molecoms.oracle.repository;

import com.pacs.molecoms.oracle.entity.StudyEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OracleStudyRepository extends JpaRepository<StudyEntity, Long> {

    // pid LIKE %...% AND delFlag = 0
    List<StudyEntity> findByPidContainingIgnoreCaseAndDelFlag(String pidPart, Long delFlag, Sort sort);

    // pid LIKE %...% AND delFlag IS NULL
    List<StudyEntity> findByPidContainingIgnoreCaseAndDelFlagIsNull(String pidPart, Sort sort);
}
