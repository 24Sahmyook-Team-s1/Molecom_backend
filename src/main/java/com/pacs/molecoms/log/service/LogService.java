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
                .actor(logReq.actor())
                .target(logReq.target())
                .db(logReq.db())
                .logAction(logReq.logAction())
                .build();
        logRepository.save(log);
    }

    public void saveLog(LoginReq request, DBlist db, LogAction logAction) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new MolecomsException(ErrorCode.USER_NOT_FOUND,"해당 이메일이 존재하지 않습니다."));
        LogReq logReq = new LogReq(user, user, db, logAction);
        saveLog(logReq);
    }

    public void saveLog(Long id, DBlist db, LogAction logAction) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new MolecomsException(ErrorCode.USER_NOT_FOUND,"해당 userID가 존재하지 않습니다."));
        LogReq logReq = new LogReq(user, user, db, logAction);
        saveLog(logReq);
    }

    public void saveLog(User user, DBlist db, LogAction logAction) {
        LogReq logReq = new LogReq(user, user, db, logAction);
        saveLog(logReq);
    }

    public void saveLog(Long actorId, Long id, DBlist db, LogAction logAction) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new MolecomsException(ErrorCode.USER_NOT_FOUND,"해당 target_userID가 존재하지 않습니다."));
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new MolecomsException(ErrorCode.USER_NOT_FOUND,"해당 actor_userID가 존재하지 않습니다."));
        LogReq logReq = new LogReq(actor, target, db, logAction);
        saveLog(logReq);
    }

    public void saveLog(User actor, Long id, DBlist db, LogAction logAction) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new MolecomsException(ErrorCode.USER_NOT_FOUND,"해당 target_userID가 존재하지 않습니다."));
        LogReq logReq = new LogReq(actor, target, db, logAction);
        saveLog(logReq);
    }
}
