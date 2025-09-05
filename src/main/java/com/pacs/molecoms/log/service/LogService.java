package com.pacs.molecoms.log.service;

import com.pacs.molecoms.exception.ErrorCode;
import com.pacs.molecoms.exception.MolecomsException;
import com.pacs.molecoms.log.dto.ReportLogRes;
import com.pacs.molecoms.log.dto.UserLogReq;
import com.pacs.molecoms.log.dto.UserLogRes;
import com.pacs.molecoms.mysql.entity.*;
import com.pacs.molecoms.mysql.repository.ReportLogRepository;
import com.pacs.molecoms.mysql.repository.UserLogRepository;
import com.pacs.molecoms.mysql.repository.UserRepository;
import com.pacs.molecoms.user.dto.LoginReq;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LogService {
    private final UserLogRepository userLogRepository;
    private final ReportLogRepository reportLogRepository;
    private final UserRepository userRepository;

    /* ========== UserLog 관련 ========== */
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
        return page.map(this::toUserRes);
    }

    private UserLogRes toUserRes(UserLog l) {
        return new UserLogRes(
                l.getActor().getEmail(),
                l.getTarget().getEmail(),
                l.getUserLogAction(),
                l.getCreatedAt()
        );
    }

    public void saveLog(LoginReq request, DBlist db, UserLogAction userLogAction) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new MolecomsException(ErrorCode.USER_NOT_FOUND,"해당 이메일이 존재하지 않습니다."));
        saveLog(new UserLogReq(user, user, db, userLogAction));
    }

    public void saveLog(Long id, DBlist db, UserLogAction userLogAction) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new MolecomsException(ErrorCode.USER_NOT_FOUND,"해당 userID가 존재하지 않습니다."));
        saveLog(new UserLogReq(user, user, db, userLogAction));
    }

    public void saveLog(User user, DBlist db, UserLogAction userLogAction) {
        saveLog(new UserLogReq(user, user, db, userLogAction));
    }

    public void saveLog(Long actorId, Long id, DBlist db, UserLogAction userLogAction) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new MolecomsException(ErrorCode.USER_NOT_FOUND,"target_userID 없음"));
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new MolecomsException(ErrorCode.USER_NOT_FOUND,"actor_userID 없음"));
        saveLog(new UserLogReq(actor, target, db, userLogAction));
    }

    public void saveLog(User actor, Long id, DBlist db, UserLogAction userLogAction) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new MolecomsException(ErrorCode.USER_NOT_FOUND,"target_userID 없음"));
        saveLog(new UserLogReq(actor, target, db, userLogAction));
    }

    /* ========== ReportLog 관련 ========== */
//    public List<ReportLogRes> getAllReportLogs() {
//        return reportLogRepository.findAll().stream()
//                .map(l -> new ReportLogRes(
//                        l.getUser().getEmail(),
//                        l.getAction(),
//                        l.getDetail(),
//                        l.getCreatedAt()
//                ))
//                .toList();
//    }

    public Page<ReportLogRes> reportLogList(Pageable pageable) {
        Page<ReportLog> page = reportLogRepository.findAll(pageable);
        return page.map(this::toReportRes);
    }

    private ReportLogRes toReportRes(ReportLog l) {
        return new ReportLogRes(
                l.getUser().getEmail(),
                l.getAction(),
                l.getDetail(),
                l.getCreatedAt()
        );
    }
}
