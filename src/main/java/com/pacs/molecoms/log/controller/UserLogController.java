package com.pacs.molecoms.log.controller;


import com.pacs.molecoms.log.service.LogService;
import com.pacs.molecoms.mysql.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class UserLogController {
    private final LogRepository logrepository;
    private final LogService logService;



}
