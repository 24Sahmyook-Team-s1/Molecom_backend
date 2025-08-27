// com/pacs/molecoms/dto/ReportResponse.java
package com.pacs.molecoms.dto;

import com.pacs.molecoms.mysql.entity.Report;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponse {
    private Long id;
    private Long studyKey;
    private Long seriesKey;
    private String modality;
    private String bodyPart;
    private String content;
    private String studyUid;
    private LocalDateTime createdAt;

    public static ReportResponse from(Report entity) {
        return ReportResponse.builder()
                .id(entity.getId())
                .studyKey(entity.getStudyKey())
                .seriesKey(entity.getSeriesKey())
                .modality(entity.getModality())
                .bodyPart(entity.getBodyPart())
                .content(entity.getContent())
                .studyUid(entity.getStudyUid())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
