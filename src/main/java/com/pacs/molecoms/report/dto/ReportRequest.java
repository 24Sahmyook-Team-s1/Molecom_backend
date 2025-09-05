package com.pacs.molecoms.report.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportRequest {
    private Long studyKey;
    private Long seriesKey;
    private String modality;
    private String bodyPart;
    private String content;
    private String studyUid;
    private Long patientId;  // ✅ 추가
    private Long authorId;   // ✅ 추가
}
