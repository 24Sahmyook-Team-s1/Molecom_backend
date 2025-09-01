// com/pacs/molecoms/oracle/dto/StudySummaryDto.java
package com.pacs.molecoms.dicomfile.dto;

import com.pacs.molecoms.oracle.entity.StudyEntity;

public record StudySummaryDto(
        Long studyKey,
        String studyInsUid,
        String studyDesc,
        String pid,
        String pname,
        String studyDate,
        String studyTime,
        String modality,
        Long seriesCnt,
        Long imageCnt
) {
    public static StudySummaryDto from(StudyEntity e) {
        return new StudySummaryDto(
                e.getStudyKey(),
                e.getStudyInsUid(),
                e.getStudyDesc(),
                e.getPid(),
                e.getPname(),
                e.getStudyDate(),
                e.getStudyTime(),
                e.getModality(),
                e.getSeriesCnt(),
                e.getImageCnt()
        );
    }
}
