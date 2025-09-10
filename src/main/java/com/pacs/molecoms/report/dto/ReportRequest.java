package com.pacs.molecoms.report.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportRequest {
    private Long studyKey;
    private String content;
}
