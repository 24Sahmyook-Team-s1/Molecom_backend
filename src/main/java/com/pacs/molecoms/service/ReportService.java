// com/pacs/molecoms/service/ReportService.java
package com.pacs.molecoms.service;

import com.pacs.molecoms.dto.ReportRequest;
import com.pacs.molecoms.dto.ReportResponse;
import com.pacs.molecoms.mysql.entity.Report;
import com.pacs.molecoms.mysql.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    @Transactional
    public ReportResponse saveReport(ReportRequest req) {
        Report report = Report.builder()
                .studyKey(req.getStudyKey())
                .seriesKey(req.getSeriesKey())
                .modality(req.getModality())
                .bodyPart(req.getBodyPart())
                .content(req.getContent())
                .studyUid(req.getStudyUid()) // ✅ optional
                .build();

        Report saved = reportRepository.save(report);
        return ReportResponse.from(saved);
    }
}
