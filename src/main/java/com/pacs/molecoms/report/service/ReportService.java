package com.pacs.molecoms.report.service;

import com.pacs.molecoms.report.dto.ReportRequest;
import com.pacs.molecoms.report.dto.ReportResponse;
import com.pacs.molecoms.mysql.entity.Report;
import com.pacs.molecoms.mysql.entity.User;
import com.pacs.molecoms.mysql.repository.ReportRepository;
import com.pacs.molecoms.mysql.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    // ✅ Report 저장
    public ReportResponse saveReport(ReportRequest request) {
        User author = userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + request.getAuthorId()));

        Report report = Report.builder()
                .studyKey(request.getStudyKey())
                .seriesKey(request.getSeriesKey())
                .modality(request.getModality())
                .bodyPart(request.getBodyPart())
                .content(request.getContent())
                .studyUid(request.getStudyUid())
                .patientId(request.getPatientId())
                .author(author)
                .build();

        Report saved = reportRepository.save(report);

        return mapToResponse(saved);
    }

    // ✅ 단일 조회
    public ReportResponse getReport(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found with id: " + id));
        return mapToResponse(report);
    }

    // ✅ Study 단위 조회
    public List<ReportResponse> getReportsByStudy(Long studyKey) {
        return reportRepository.findByStudyKey(studyKey).stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ✅ Study + Series 단위 조회
    public List<ReportResponse> getReportsByStudyAndSeries(Long studyKey, Long seriesKey) {
        return reportRepository.findByStudyKeyAndSeriesKey(studyKey, seriesKey).stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ✅ Entity → DTO 변환 공통 메서드
    private ReportResponse mapToResponse(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .studyKey(report.getStudyKey())
                .seriesKey(report.getSeriesKey())
                .modality(report.getModality())
                .bodyPart(report.getBodyPart())
                .content(report.getContent())
                .studyUid(report.getStudyUid())
                .patientId(report.getPatientId())
                .authorId(report.getAuthor().getId())
                .build();
    }
}
