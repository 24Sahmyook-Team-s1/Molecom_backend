// com/pacs/molecoms/oracle/service/OracleStudyReadService.java
package com.pacs.molecoms.dicomfile.service;

import com.pacs.molecoms.dicomfile.dto.StudySummaryDto;
import com.pacs.molecoms.oracle.entity.StudyEntity;
import com.pacs.molecoms.oracle.repository.OracleStudyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class OracleStudyReadService {

    private final OracleStudyRepository studyRepo;

    @Transactional(readOnly = true, transactionManager = "oracleTx")
    public List<StudySummaryDto> findStudiesByPatientUidLike(String pidPart) {
        // 문자열(YYYYMMDD/HHmmss)라면 desc 정렬로 최신 우선
        Sort sort = Sort.by(Sort.Order.desc("studyDate"), Sort.Order.desc("studyTime"));

        List<StudyEntity> valid = studyRepo
                .findByPidContainingIgnoreCaseAndDelFlag(pidPart, 0L, sort);
        List<StudyEntity> nulls = studyRepo
                .findByPidContainingIgnoreCaseAndDelFlagIsNull(pidPart, sort);

        // (delFlag=0) + (delFlag IS NULL) 합치면서 중복 제거(키 기준)
        var merged = Stream.concat(valid.stream(), nulls.stream())
                .collect(LinkedHashMap<Long, StudyEntity>::new,
                        (m, e) -> m.putIfAbsent(e.getStudyKey(), e),
                        (m1, m2) -> m2.forEach(m1::putIfAbsent))
                .values().stream().toList();

        return merged.stream().map(StudySummaryDto::from).toList();
    }
}