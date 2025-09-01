// com/pacs/molecoms/oracle/entity/StudyEntity.java
package com.pacs.molecoms.oracle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

@Getter
@Entity
@Table(name = "STUDYTAB")
@Immutable
public class StudyEntity {

    @Id
    @Column(name = "STUDYKEY")
    private Long studyKey;

    @Column(name = "STUDYINSUID")
    private String studyInsUid;

    @Column(name = "PATIENTKEY")
    private Long patientKey;

    @Column(name = "STUDYDESC")
    private String studyDesc;

    @Column(name = "PID")
    private String pid;

    @Column(name = "PNAME")
    private String pname;

    // Oracle에 보통 문자열(YYYYMMDD / HHmmss) 형태라 우선 String으로
    @Column(name = "STUDYDATE")
    private String studyDate;

    @Column(name = "STUDYTIME")
    private String studyTime;

    @Column(name = "MODALITY")
    private String modality;

    @Column(name = "SERIESCNT")
    private Long seriesCnt;

    @Column(name = "IMAGECNT")
    private Long imageCnt;

    @Column(name = "DELFLAG")
    private Long delFlag; // 0:유효, 1:삭제, NULL:과거 데이터일 수 있음
}
