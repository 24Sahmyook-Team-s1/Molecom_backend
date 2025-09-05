package com.pacs.molecoms.log.service;

import com.pacs.molecoms.exception.ErrorCode;
import com.pacs.molecoms.exception.MolecomsException;
import com.pacs.molecoms.log.dto.LogReq;
import com.pacs.molecoms.mysql.entity.DBlist;
import com.pacs.molecoms.mysql.entity.UserLogAction;
import com.pacs.molecoms.mysql.entity.User;
import com.pacs.molecoms.mysql.repository.LogRepository;
import com.pacs.molecoms.mysql.entity.UserLog;
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
        UserLog userLog = UserLog.builder()
                .actor(logReq.actor())
                .target(logReq.target())
                .db(logReq.db())
                .userLogAction(logReq.userLogAction())
                .build();
        logRepository.save(userLog);
    }

    public void saveLog(LoginReq request, DBlist db, UserLogAction userLogAction) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new MolecomsException(ErrorCode.USER_NOT_FOUND,"해당 이메일이 존재하지 않습니다."));
        LogReq logReq = new LogReq(user, user, db, userLogAction);
        saveLog(logReq);
    }

    public void saveLog(Long id, DBlist db, UserLogAction userLogAction) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new MolecomsException(ErrorCode.USER_NOT_FOUND,"해당 userID가 존재하지 않습니다."));
        LogReq logReq = new LogReq(user, user, db, userLogAction);
        saveLog(logReq);
    }

    public void saveLog(User user, DBlist db, UserLogAction userLogAction) {
        LogReq logReq = new LogReq(user, user, db, userLogAction);
        saveLog(logReq);
    }

    public void saveLog(Long actorId, Long id, DBlist db, UserLogAction userLogAction) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new MolecomsException(ErrorCode.USER_NOT_FOUND,"해당 target_userID가 존재하지 않습니다."));
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new MolecomsException(ErrorCode.USER_NOT_FOUND,"해당 actor_userID가 존재하지 않습니다."));
        LogReq logReq = new LogReq(actor, target, db, userLogAction);
        saveLog(logReq);
    }

    public void saveLog(User actor, Long id, DBlist db, UserLogAction userLogAction) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new MolecomsException(ErrorCode.USER_NOT_FOUND,"해당 target_userID가 존재하지 않습니다."));
        LogReq logReq = new LogReq(actor, target, db, userLogAction);
        saveLog(logReq);
    }
}
