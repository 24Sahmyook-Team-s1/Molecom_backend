// com/pacs/molecoms/dto/ReportRequest.java
package com.pacs.molecoms.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportRequest {
    private Long studyKey;
    private Long seriesKey;
    private String modality;
    private String bodyPart;
    private String content;

    // ✅ Swagger에서 optional
    private String studyUid;
}
