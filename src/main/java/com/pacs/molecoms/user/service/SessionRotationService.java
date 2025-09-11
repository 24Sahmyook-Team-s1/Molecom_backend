// src/main/java/com/pacs/molecoms/user/service/SessionRotationService.java
package com.pacs.molecoms.user.service;

import com.pacs.molecoms.mysql.repository.AuthSessionRepository;
import com.pacs.molecoms.mysql.repository.UserRepository;
import com.pacs.molecoms.security.CookieUtil;
import com.pacs.molecoms.security.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionRotationService {

    private final AuthSessionRepository sessionRepo;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /**
     * refreshJti가 DB 세션과 일치하면 새 Access/Refresh를 발급하고 회전(rotate)한다.
     * 실패(만료/불일치) 시 Optional.empty().
     */
    @Transactional
    public Optional<String> rotateIfValid(Long userId, String refreshJti,
                                          CookieUtil cookieUtil, HttpServletResponse res) {

        var sessOpt = sessionRepo.findWithLockingByUser_Id(userId);
        if (sessOpt.isEmpty()) return Optional.empty();
        var sess = sessOpt.get();

        // 리프레시 만료일 경과 → 세션 만료(EXPIRED)
        if (sess.getRefreshExpireAt().isBefore(LocalDateTime.now())) {
            sess.expire();
            sessionRepo.save(sess);
            return Optional.empty();
        }

        // 유효성 체크
        if (!sess.isActive() || !refreshJti.equals(sess.getRefreshJti())) {
            return Optional.empty();
        }

        // 실제 유저 조회 후 새 토큰 발급
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("세션 유저를 찾을 수 없음: " + userId));

        String newAccess  = jwtUtil.generateAccessToken(user);
        String newRefresh = jwtUtil.generateRefreshToken(user);

        // DB 세션 회전(rotate): jti/만료 갱신
        sess.rotate(
                jwtUtil.getJti(newAccess),
                jwtUtil.getJti(newRefresh),
                jwtUtil.getExpiration(newAccess).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                jwtUtil.getExpiration(newRefresh).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        );
        sessionRepo.save(sess);

        // 쿠키 교체 (로컬 개발 기준: Secure=false, SameSite=Lax)
        cookieUtil.addJwtCookie(res, "accessToken", newAccess, false);
        cookieUtil.addJwtCookie(res, "refreshToken", newRefresh, false);

        return Optional.of(newAccess);
    }
}
