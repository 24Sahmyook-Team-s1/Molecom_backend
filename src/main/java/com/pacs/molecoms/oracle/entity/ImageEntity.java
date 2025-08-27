package com.pacs.molecoms.oracle.entity;

import com.pacs.molecoms.oracle.entity.id.ImageId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "IMAGETAB")
@IdClass(ImageId.class)
@Getter @Setter
@Immutable // 읽기 전용
public class ImageEntity {

    @Id
    @Column(name = "studykey", nullable = false)
    private Long studyKey;

    @Id
    @Column(name = "serieskey", nullable = false)
    private Long seriesKey;

    @Id
    @Column(name = "imagekey", nullable = false)
    private Long imageKey;

    @Column(name = "sopinstanceuid", length = 128, nullable = false)
    private String sopInstanceUid;

    @Column(name = "sopclassuid", length = 128)
    private String sopClassUid;

    @Column(name = "path", length = 1024)
    private String path;

    @Column(name = "fname", length = 255)
    private String fname;

    @Column(name = "DELFLAG")
    private Long delFlag; // 0:유효, 1:삭제, NULL:과거 데이터일 수 있음

    /** 선택: SeriesEntity 참조 (읽기 전용 참조) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "studykey", referencedColumnName = "studykey", insertable = false, updatable = false),
            @JoinColumn(name = "serieskey", referencedColumnName = "serieskey", insertable = false, updatable = false)
    })
    private SeriesEntity series;
}
