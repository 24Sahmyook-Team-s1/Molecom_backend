package com.pacs.molecoms.log.service;

import com.pacs.molecoms.exception.ErrorCode;
import com.pacs.molecoms.exception.MolecomsException;
import com.pacs.molecoms.log.dto.LogReq;
import com.pacs.molecoms.mysql.entity.DBlist;
import com.pacs.molecoms.mysql.entity.LogAction;
import com.pacs.molecoms.mysql.entity.User;
import com.pacs.molecoms.mysql.repository.LogRepository;
import com.pacs.molecoms.mysql.entity.Log;
import com.pacs.molecoms.mysql.repository.UserRepository;
import com.pacs.molecoms.user.dto.LoginReq;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class LogService {
    private final LogRepository logRepository;
    private final UserRepository userRepository;

    public void saveLog(LogReq logReq) {
        Log log = Log.builder()
                .user(logReq.user())
                .db(logReq.db())
                .logAction(logReq.logAction())
                .build();
        logRepository.save(log);
    }

    public void saveLog(LoginReq request, DBlist db, LogAction logAction) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new MolecomsException(ErrorCode.USER_NOT_FOUND,"해당 이메일이 존재하지 않습니다."));
        LogReq logReq = new LogReq(user, db, logAction);
        saveLog(logReq);
    }

    public void saveLog(Long id, DBlist db, LogAction logAction) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new MolecomsException(ErrorCode.USER_NOT_FOUND,"해당 ID가 존재하지 않습니다."));
        LogReq logReq = new LogReq(user, db, logAction);
        saveLog(logReq);
    }
}
