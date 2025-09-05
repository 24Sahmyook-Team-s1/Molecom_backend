package com.pacs.molecoms.log.controller;


import com.pacs.molecoms.log.dto.DicomLogRes;
import com.pacs.molecoms.log.dto.UserLogRes;
import com.pacs.molecoms.log.service.DicomLogService;
import com.pacs.molecoms.log.service.LogService;
import com.pacs.molecoms.mysql.entity.ReportLog;
import com.pacs.molecoms.mysql.repository.ReportLogRepository;
import com.pacs.molecoms.mysql.repository.UserLogRepository;
import com.pacs.molecoms.mysql.repository.UserRepository;
import com.pacs.molecoms.report.dto.ReportResponse;
import com.pacs.molecoms.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LogController {
    private final UserLogRepository userLogrepository;
    private final LogService logService;
    private final ReportLogRepository reportLogRepository;
    private final ReportService reportService;
    private final DicomLogService dicomLogService;

    @Operation(summary = "모든 유저 로그 불러오기")
    @GetMapping("/users/logAll")
    public ResponseEntity<Page<UserLogRes>> allLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        Sort sortObj = Sort.by(sort.split(",")[0]).descending();
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return ResponseEntity.ok(logService.logList(pageable));
    }

    // ✅ ReportLog 전체 조회 (DTO 변환 포함)
    @GetMapping("/reports/logAll")
    public ResponseEntity<List<ReportResponse>> getAllReportLogs() {
        List<ReportLog> logs = reportLogRepository.findAll();

        // ReportLog 내부에 Report 엔티티가 있다면 → reportLog.getReport() 로 꺼내서 변환
        List<ReportResponse> responses = logs.stream()
                .map(log -> reportService.mapToResponse(log.getReport())) // ReportLog -> Report -> ReportResponse
                .toList();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "모든 파일 관련 로그 불러오기")
    @GetMapping("/dicom/logAll")
    public ResponseEntity<Page<DicomLogRes>> getAllDicomLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        Sort sortObj = Sort.by(sort.split(",")[0]).descending();
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return ResponseEntity.ok(dicomLogService.logList(pageable));
    }

}


