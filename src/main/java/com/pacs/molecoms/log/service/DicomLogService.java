package com.pacs.molecoms.log.service;

import com.pacs.molecoms.exception.ErrorCode;
import com.pacs.molecoms.exception.MolecomsException;
import com.pacs.molecoms.mysql.entity.DicomLog;
import com.pacs.molecoms.mysql.entity.DicomLogAction;
import com.pacs.molecoms.mysql.entity.User;
import com.pacs.molecoms.mysql.repository.DicomLogRepository;
import com.pacs.molecoms.mysql.repository.UserRepository;
import com.pacs.molecoms.security.CookieUtil;
import com.pacs.molecoms.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DicomLogService {
    private final DicomLogRepository dicomLogRepository;
    private final UserRepository userRepository;
    private final CookieUtil cookieUtil;
    private final JwtUtil jwtUtil;

    private User getActor(HttpServletRequest request) {
        String token = cookieUtil.getTokenFromCookie(request, "accessToken");
        String uidStr = jwtUtil.getUserIdFromToken(token);
        return userRepository.findByEmail(uidStr)
                .orElseThrow(() -> new MolecomsException(ErrorCode.USER_NOT_FOUND, "actor 없음"));
    }

    @Transactional
    public void saveLog(HttpServletRequest request, String targetUid, DicomLogAction action) {
        User actor = getActor(request);
        DicomLog log = DicomLog.builder()
                .actor(actor)
                .targetUid(targetUid)
                .action(action)
                .build();
        dicomLogRepository.save(log);
    }
}

