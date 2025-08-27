// com/pacs/molecoms/dicomfile/service/DicomFileFetchServiceDummy.java
package com.pacs.molecoms.dicomfile.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
@Profile("local")  // ✅ 로컬 프로필에서만 로딩
public class DicomFileFetchServiceDummy extends DicomFileFetchService {

    public DicomFileFetchServiceDummy() {
        super(null, null); // Oracle 전용 Repo/Service는 안 씀
    }

    @Override
    public InputStream openDicomStream(Long studyKey, Long seriesKey, Long imageKey) throws IOException {
        // 더미 데이터 → 실제 DICOM 파일 대신 문자열 스트림
        return new ByteArrayInputStream("DICOM-DUMMY-DATA".getBytes());
    }

    @Override
    public String getFileName(Long studyKey, Long seriesKey, Long imageKey) {
        return "dummy.dcm"; // 항상 같은 파일명
    }
}
