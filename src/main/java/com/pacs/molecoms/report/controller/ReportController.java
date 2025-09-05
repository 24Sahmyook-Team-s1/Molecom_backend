package com.pacs.molecoms.report.controller;

import com.pacs.molecoms.report.dto.ReportRequest;
import com.pacs.molecoms.report.dto.ReportResponse;
import com.pacs.molecoms.report.service.ReportService;
import com.pacs.molecoms.mysql.entity.ReportLog;
import com.pacs.molecoms.mysql.repository.ReportLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ReportLogRepository reportLogRepository; // ✅ 로그 조회용

    // ✅ Report 생성
    @PostMapping
    public ResponseEntity<ReportResponse> createReport(@RequestBody ReportRequest request) {
        return ResponseEntity.ok(reportService.saveReport(request));
    }

    // ✅ 단일 Report 조회 (id 기준)
    @GetMapping("/{id}")
    public ResponseEntity<ReportResponse> getReport(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getReport(id));
    }

    // ✅ Study 단위로 Report 목록 조회
    @GetMapping("/study/{studyKey}")
    public ResponseEntity<List<ReportResponse>> getReportsByStudy(@PathVariable Long studyKey) {
        return ResponseEntity.ok(reportService.getReportsByStudy(studyKey));
    }

    // ✅ Study + Series 단위로 Report 조회
    @GetMapping("/study/{studyKey}/series/{seriesKey}")
    public ResponseEntity<List<ReportResponse>> getReportsByStudyAndSeries(
            @PathVariable Long studyKey,
            @PathVariable Long seriesKey) {
        return ResponseEntity.ok(reportService.getReportsByStudyAndSeries(studyKey, seriesKey));
    }

    // ✅ ReportLog 전체 조회
    @GetMapping("/logs")
    public ResponseEntity<List<ReportLog>> getAllReportLogs() {
        return ResponseEntity.ok(reportLogRepository.findAll());
    }
}
