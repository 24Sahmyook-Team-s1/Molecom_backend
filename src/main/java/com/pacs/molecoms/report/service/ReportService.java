package com.pacs.molecoms.report.service;

import com.pacs.molecoms.report.dto.ReportRequest;
import com.pacs.molecoms.report.dto.ReportResponse;
import com.pacs.molecoms.mysql.entity.*;
import com.pacs.molecoms.mysql.repository.ReportLogRepository;
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
    private final ReportLogRepository reportLogRepository;

    // ✅ Report 저장 + 로그 기록
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

        // ✅ 로그 저장 (Report 생성)
        reportLogRepository.save(ReportLog.builder()
                .user(author)
                .report(saved)
                .action(ReportAction.CREATE)
                .detail("Report created: studyKey=" + saved.getStudyKey())
                .build()
        );

        return mapToResponse(saved);
    }

    // ✅ Report 단일 조회 + 로그 기록
    public ReportResponse getReport(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found with id: " + id));

        // ✅ 로그 저장 (Report 조회)
        reportLogRepository.save(ReportLog.builder()
                .user(report.getAuthor()) // ⚠️ 현재는 작성자 기준, 실제 운영 시엔 로그인한 사용자 기준으로 변경 권장
                .report(report)
                .action(ReportAction.VIEW)
                .detail("Report viewed")
                .build()
        );

        return mapToResponse(report);
    }

    // ✅ Study 단위 조회
    public List<ReportResponse> getReportsByStudy(Long studyKey) {
        List<Report> reports = reportRepository.findByStudyKey(studyKey);

        // 필요하다면 여기서도 VIEW 로그 기록 가능
        return reports.stream().map(this::mapToResponse).toList();
    }

    // ✅ Study + Series 단위 조회
    public List<ReportResponse> getReportsByStudyAndSeries(Long studyKey, Long seriesKey) {
        List<Report> reports = reportRepository.findByStudyKeyAndSeriesKey(studyKey, seriesKey);

        // 필요하다면 여기서도 VIEW 로그 기록 가능
        return reports.stream().map(this::mapToResponse).toList();
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
