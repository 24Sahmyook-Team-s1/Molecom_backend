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

    @Column(name = "modality", nullable = false)
    private String modality;

    @Column(name = "body_part", nullable = false)
    private String bodyPart;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "study_uid", nullable = false)
    private String studyUid;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    // ✅ User와 연관관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
