package com.pacs.molecoms.log.controller;

import com.pacs.molecoms.exception.ErrorCode;
import com.pacs.molecoms.exception.MolecomsException;
import com.pacs.molecoms.log.dto.LogReq;
import com.pacs.molecoms.log.dto.LogRes;
import com.pacs.molecoms.log.service.LogService;
import com.pacs.molecoms.mysql.entity.*;
import com.pacs.molecoms.mysql.repository.UserRepository;
import com.pacs.molecoms.user.dto.*;
import com.pacs.molecoms.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {
    private final LogService logService;

//    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "모든 로그 불러오기")
    @GetMapping("/logAll")
    public ResponseEntity<Page<LogRes>> allLogs(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "id,desc") String sort) {

        Sort sortObj = Sort.by(sort.split(",")[0]).descending();
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return ResponseEntity.ok(logService.logList(pageable));
    }
}


//@PreAuthorize("hasRole('ADMIN')")
//@Operation(summary = "유저 목록")
//@GetMapping
//public ResponseEntity<Page<UserRes>> list(
//        @RequestParam(required = false) UserStatus status,
//        @RequestParam(defaultValue = "0") int page,
//        @RequestParam(defaultValue = "20") int size,
//        @RequestParam(defaultValue = "id,desc") String sort) {
//
//    Sort sortObj = Sort.by(sort.split(",")[0])
//            .descending();
//    Pageable pageable = PageRequest.of(page, size, sortObj);
//    return ResponseEntity.ok(service.list(status, pageable));
//}