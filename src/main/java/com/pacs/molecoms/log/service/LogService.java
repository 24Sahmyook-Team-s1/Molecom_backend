package com.pacs.molecoms.log.service;

import com.pacs.molecoms.log.dto.LogReq;
import com.pacs.molecoms.log.repository.LogRepository;
import com.pacs.molecoms.mysql.entity.Log;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogService {
    LogRepository logRepository;

    public void saveLog(LogReq logReq) {
        Log log = Log.builder()
                .user(logReq.user())
                .db(logReq.db())
                .logAction(logReq.logAction())
                .build();
        logRepository.save(log);
    }

}
