package com.pacs.molecoms.oracle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "IMAGETAB")
@IdClass(OracleImageId.class)
@Getter @Setter
@Immutable // 읽기 전용
public class OracleImage {

    @Id @Column(name = "STUDYKEY")  private Long studyKey;
    @Id @Column(name = "SERIESKEY") private Long seriesKey;
    @Id @Column(name = "IMAGEKEY")  private Long imageKey;

    @Column(name = "PATH")   private String path;   // 디렉터리
    @Column(name = "FNAME")  private String fname;  // 파일명

    // (필요하면 추가 컬럼 매핑)
}
