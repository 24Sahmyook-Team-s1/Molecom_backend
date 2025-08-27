package com.pacs.molecoms.dicomfile.service;

import com.pacs.molecoms.oracle.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class DicomFileFetchService {

    private final ImageRepository oracleImageRepo;
    private final CifsFileService cifsFileService;

    /** 복합키로 조회 → SMB 스트림 오픈 */
    @Transactional(readOnly = true, transactionManager = "oracleTx")
    public InputStream openDicomStream(Long studyKey, Long seriesKey, Long imageKey) throws IOException {
        var img = oracleImageRepo.findByStudyKeyAndSeriesKeyAndImageKey(studyKey, seriesKey, imageKey)
                .orElseThrow(() -> new IllegalArgumentException(
                        "IMAGE not found: studyKey=" + studyKey + ", seriesKey=" + seriesKey + ", imageKey=" + imageKey));
        return cifsFileService.openStream(img.getPath(), img.getFname());
    }

    /** 다운로드 파일명 */
    @Transactional(readOnly = true, transactionManager = "oracleTx")
    public String getFileName(Long studyKey, Long seriesKey, Long imageKey) {
        return oracleImageRepo.findByStudyKeyAndSeriesKeyAndImageKey(studyKey, seriesKey, imageKey)
                .map(i -> i.getFname())
                .orElseThrow(() -> new IllegalArgumentException(
                        "IMAGE not found: studyKey=" + studyKey + ", seriesKey=" + seriesKey + ", imageKey=" + imageKey));
    }
}
