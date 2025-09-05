package com.pacs.molecoms.log.controller;


import com.pacs.molecoms.log.dto.ReportLogRes;
import com.pacs.molecoms.log.dto.UserLogRes;
import com.pacs.molecoms.log.service.LogService;
import com.pacs.molecoms.mysql.entity.ReportLog;
import com.pacs.molecoms.mysql.repository.ReportLogRepository;
import com.pacs.molecoms.mysql.repository.UserLogRepository;
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

    @Operation(summary = "모든 유저 로그 불러오기")
    @GetMapping("/users/logAll")
    public ResponseEntity<Page<UserLogRes>> allLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdat,desc") String sort) {

        Sort sortObj = Sort.by(sort.split(",")[0]).descending();
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return ResponseEntity.ok(logService.logList(pageable));
    }


    @Operation(summary = "모든 Report 로그 불러오기")
    @GetMapping("/reports/logAll")
    public ResponseEntity<List<ReportLogRes>> getAllReportLogs() {
        List<ReportLogRes> responses = reportLogRepository.findAll().stream()
                .map(this::toRes) // ReportLog -> ReportLogRes 변환
                .toList();
        return ResponseEntity.ok(responses);
    }

    // ReportLog -> ReportLogRes 변환 메서드
    private ReportLogRes toRes(ReportLog log) {
        return new ReportLogRes(
                log.getId(),
                log.getUser().getEmail(),
                log.getReport().getId(),
                log.getAction(),
                log.getDetail(),
                log.getCreatedAt()
        );
    }

}


