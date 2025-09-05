package com.pacs.molecoms.log.controller;


import com.pacs.molecoms.log.dto.DicomLogRes;

import com.pacs.molecoms.log.dto.ReportLogRes;

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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;
    private final ReportLogRepository reportLogRepository;
    private final ReportService reportService;
    private final DicomLogService dicomLogService;

    @Operation(summary = "모든 유저 로그 불러오기")
    @GetMapping("/users/logAll")
    public ResponseEntity<Page<UserLogRes>> allUserLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
      
        Sort sortObj = Sort.by(sort.split(",")[0]).descending();
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return ResponseEntity.ok(logService.logList(pageable));
    }

    @Operation(summary = "모든 Report 로그 불러오기")
    @GetMapping("/reports/logAll")
    public ResponseEntity<Page<ReportLogRes>> allReportLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        Sort sortObj = Sort.by(sort.split(",")[0]).descending();
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return ResponseEntity.ok(logService.reportLogList(pageable));
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
