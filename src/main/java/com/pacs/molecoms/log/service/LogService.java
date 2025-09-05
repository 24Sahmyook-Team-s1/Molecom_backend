package com.pacs.molecoms.log.service;

import com.pacs.molecoms.exception.ErrorCode;
import com.pacs.molecoms.exception.MolecomsException;
import com.pacs.molecoms.log.dto.UserLogReq;
import com.pacs.molecoms.log.dto.UserLogRes;
import com.pacs.molecoms.mysql.entity.DBlist;
import com.pacs.molecoms.mysql.entity.UserLogAction;
import com.pacs.molecoms.mysql.entity.User;
import com.pacs.molecoms.mysql.repository.UserLogRepository;
import com.pacs.molecoms.mysql.entity.UserLog;
import com.pacs.molecoms.mysql.repository.UserRepository;
import com.pacs.molecoms.user.dto.LoginReq;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class LogService {
    private final UserLogRepository userLogRepository;
    private final UserRepository userRepository;

    public void saveLog(UserLogReq userLogReq) {
        UserLog userLog = UserLog.builder()
                .actor(userLogReq.actor())
                .target(userLogReq.target())
                .db(userLogReq.db())
                .userLogAction(userLogReq.userLogAction())
                .build();
        userLogRepository.save(userLog);
    }

    public Page<UserLogRes> logList(Pageable pageable) {
        Page<UserLog> page = userLogRepository.findAll(pageable);
        return page.map(this::toRes);
    }

    private UserLogRes toRes(UserLog l) {
        return new UserLogRes(
                l.getActor().getEmail(),l.getTarget().getEmail(), l.getUserLogAction(), l.getCreatedAt()
        );
    }


    public void saveLog(LoginReq request, DBlist db, UserLogAction userLogAction) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new MolecomsException(ErrorCode.USER_NOT_FOUND,"해당 이메일이 존재하지 않습니다."));
        UserLogReq userLogReq = new UserLogReq(user, user, db, userLogAction);
        saveLog(userLogReq);
    }

    public void saveLog(Long id, DBlist db, UserLogAction userLogAction) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new MolecomsException(ErrorCode.USER_NOT_FOUND,"해당 userID가 존재하지 않습니다."));
        UserLogReq userLogReq = new UserLogReq(user, user, db, userLogAction);
        saveLog(userLogReq);
    }

    public void saveLog(User user, DBlist db, UserLogAction userLogAction) {
        UserLogReq userLogReq = new UserLogReq(user, user, db, userLogAction);
        saveLog(userLogReq);
    }

    public void saveLog(Long actorId, Long id, DBlist db, UserLogAction userLogAction) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new MolecomsException(ErrorCode.USER_NOT_FOUND,"해당 target_userID가 존재하지 않습니다."));
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new MolecomsException(ErrorCode.USER_NOT_FOUND,"해당 actor_userID가 존재하지 않습니다."));
        UserLogReq userLogReq = new UserLogReq(actor, target, db, userLogAction);
        saveLog(userLogReq);
    }

    public void saveLog(User actor, Long id, DBlist db, UserLogAction userLogAction) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new MolecomsException(ErrorCode.USER_NOT_FOUND,"해당 target_userID가 존재하지 않습니다."));
        UserLogReq userLogReq = new UserLogReq(actor, target, db, userLogAction);
        saveLog(userLogReq);
    }
}
