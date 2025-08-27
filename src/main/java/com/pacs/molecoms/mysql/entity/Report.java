// com/pacs/molecoms/mysql/entity/Report.java
package com.pacs.molecoms.mysql.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "study_key", nullable = false)
    private Long studyKey;

    @Column(name = "series_key", nullable = false)
    private Long seriesKey;

    @Column(name = "modality", nullable = false, length = 20)
    private String modality;

    @Column(name = "body_part", length = 50)
    private String bodyPart;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    // ✅ study_uid NULL 허용
    @Column(name = "study_uid", nullable = true, length = 255)
    private String studyUid;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
