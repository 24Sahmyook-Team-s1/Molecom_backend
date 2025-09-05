package com.pacs.molecoms.log.controller;

import com.pacs.molecoms.log.dto.ReportLogRes;
import com.pacs.molecoms.log.dto.UserLogRes;
import com.pacs.molecoms.log.service.LogService;
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

    @Operation(summary = "모든 유저 로그 불러오기")
    @GetMapping("/users/logAll")
    public ResponseEntity<Page<UserLogRes>> allLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) { // ✅ createdAt 오타 수정

        Sort sortObj = Sort.by(sort.split(",")[0]).descending();
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return ResponseEntity.ok(logService.logList(pageable));
    }

    @Operation(summary = "모든 Report 로그 불러오기")
    @GetMapping("/reports/logAll")
    public ResponseEntity<List<ReportLogRes>> getAllReportLogs() {
        return ResponseEntity.ok(logService.getAllReportLogs());
    }

//    @Operation(summary = "모든 Report 로그 불러오기")
//    @GetMapping("/reports/logAll")
//    public ResponseEntity<Page<ReportLogRes>> getAllReportLogs(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @RequestParam(defaultValue = "createdAt,desc") String sort) { // ✅ createdAt 오타 수정
//
//        Sort sortObj = Sort.by(sort.split(",")[0]).descending();
//        Pageable pageable = PageRequest.of(page, size, sortObj);
//        return ResponseEntity.ok(logService.logList(pageable));
//    }
}
