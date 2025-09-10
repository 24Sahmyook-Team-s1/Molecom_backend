package com.pacs.molecoms.report.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportResponse {
    private String author;
    private String content;      // 판독 내용
    private LocalDateTime createdAt; // 생성 시간
    private  LocalDateTime updatedAt;

}
