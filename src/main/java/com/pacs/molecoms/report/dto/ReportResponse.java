package com.pacs.molecoms.report.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportResponse {
    private String modality;     // 검사 종류 (예: CT, MRI)
    private String bodyPart;     // 촬영 부위
    private String content;      // 판독 내용
    private Long authorId;
    private LocalDateTime createdAt; // 생성 시간

}
