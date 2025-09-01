// com/pacs/molecoms/oracle/entity/SeriesEntity.java
package com.pacs.molecoms.oracle.entity;

import com.pacs.molecoms.oracle.entity.id.SeriesId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "seriestab",
        indexes = {
                @Index(name = "ix_series_ins_uid", columnList = "seriesinsuid"),
                @Index(name = "ix_series_studykey", columnList = "studykey")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_series_ins_uid", columnNames = {"seriesinsuid"})
        }
)
@IdClass(SeriesId.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeriesEntity {

    @Id
    @Column(name = "studykey", nullable = false)
    private Long studyKey;

    @Id
    @Column(name = "serieskey", nullable = false)
    private Long seriesKey;

    @Column(name = "studyinsuid", length = 128, nullable = false)
    private String studyInsUid;

    @Column(name = "seriesinsuid", length = 128, nullable = false)
    private String seriesInsUid;

    @Column(name = "seriesnum")
    private Integer seriesNum;

    @Column(name = "modality", length = 16)
    private String modality;

    @Column(name = "bodypart", length = 64)
    private String bodyPart;

    @Column(name = "seriesdesc", length = 512)
    private String seriesDesc;

    @Column(name = "imagecnt")
    private Long imageCnt;

    @Column(name = "DELFLAG")
    private Long delFlag; // 0:유효, 1:삭제, NULL:과거 데이터일 수 있음

    /** 선택: StudyEntity 참조 (읽기 전용 참조) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studykey", referencedColumnName = "studykey",
            insertable = false, updatable = false)
    private StudyEntity study;
}
