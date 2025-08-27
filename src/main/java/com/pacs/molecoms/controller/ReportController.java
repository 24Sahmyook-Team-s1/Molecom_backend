// com/pacs/molecoms/controller/ReportController.java
package com.pacs.molecoms.controller;

import com.pacs.molecoms.dto.ReportRequest;
import com.pacs.molecoms.dto.ReportResponse;
import com.pacs.molecoms.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "리포트 작성")
    @PostMapping
    public ResponseEntity<ReportResponse> createReport(@RequestBody ReportRequest request) {
        return ResponseEntity.ok(reportService.saveReport(request));
    }
}
