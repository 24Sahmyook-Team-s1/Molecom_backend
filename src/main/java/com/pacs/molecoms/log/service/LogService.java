package com.pacs.molecoms.log.service;

import com.pacs.molecoms.log.dto.LogReq;
import com.pacs.molecoms.mysql.repository.LogRepository;
import com.pacs.molecoms.mysql.entity.Log;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class LogService {
    private final LogRepository logRepository;

    public void saveLog(LogReq logReq) {
        Log log = Log.builder()
                .user(logReq.user())
                .db(logReq.db())
                .logAction(logReq.logAction())
                .build();
        logRepository.save(log);
    }

}
