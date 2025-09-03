package com.pacs.molecoms.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportResponse {
    private Long id;
    private Long studyKey;
    private Long seriesKey;
    private String modality;
    private String bodyPart;
    private String content;
    private String studyUid;
    private Long patientId;
    private Long authorId;
}
