// src/main/java/com/pacs/molecoms/user/service/AuthService.java
package com.pacs.molecoms.user.service;

import com.pacs.molecoms.exception.ErrorCode;
import com.pacs.molecoms.exception.MolecomsException;
import com.pacs.molecoms.mysql.entity.AuthSession;
import com.pacs.molecoms.mysql.entity.User;
import com.pacs.molecoms.mysql.repository.AuthSessionRepository;
import com.pacs.molecoms.mysql.repository.UserRepository;
import com.pacs.molecoms.security.CookieUtil;
import com.pacs.molecoms.security.JwtUtil;
import com.pacs.molecoms.user.dto.AuthRes;
import com.pacs.molecoms.user.dto.LoginReq;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final CookieUtil cookieUtil;
    private final JwtUtil jwtUtil;

    /** 로그인: 기존 세션이 있으면 TAKEN_OVER로 무효화한 뒤 새 토큰 발급/저장 */
    @Transactional
    public AuthRes login(LoginReq request, HttpServletResponse response) {
        // 1) 유저 조회 + 비밀번호 검증
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new MolecomsException(ErrorCode.USER_NOT_FOUND, "해당 이메일이 존재하지 않습니다."));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new MolecomsException(ErrorCode.PASSWORD_FAIL);
        }

        Long userId = user.getId();

        // 2) 기존 세션 잠금 조회 → 가로채기(revoke TAKEN_OVER)
        var sessOpt = sessionRepository.findWithLockingByUser_Id(userId);
        sessOpt.ifPresent(s -> {
            if (s.isActive()) {
                s.revoke("TAKEN_OVER");
                sessionRepository.save(s);
            }
        });

        // 3) 새 토큰 발급
        String accessToken  = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        String accessJti  = jwtUtil.getJti(accessToken);
        String refreshJti = jwtUtil.getJti(refreshToken);
        Date   accessExp  = jwtUtil.getExpiration(accessToken);
        Date   refreshExp = jwtUtil.getExpiration(refreshToken);

        // 4) 세션 upsert(유저당 1행 유지)
        AuthSession s = sessOpt.orElseGet(() -> AuthSession.builder().user(user).build());
        s.setActive(true);
        s.setAccessJti(accessJti);
        s.setRefreshJti(refreshJti);
        s.setAccessExpireAt(accessExp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        s.setRefreshExpireAt(refreshExp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        s.setRevokedAt(null);
        s.setRevokedReason(null);
        sessionRepository.save(s);

        // 5) 쿠키 심기 (개발 기준: Lax, Secure=false)
        cookieUtil.addJwtCookie(response, "accessToken", accessToken, false);
        cookieUtil.addJwtCookie(response, "refreshToken", refreshToken, false);

        // 6) 응답
        return new AuthRes(accessToken, refreshToken);
    }

    /** 로그아웃: 세션을 LOGOUT으로 무효화 */
    @Transactional
    public void logout(Long userId) {
        userRepository.findById(userId).ifPresent(u ->
                sessionRepository.findWithLockingByUser_Id(userId).ifPresent(s -> {
                    if (s.isActive()) s.revoke("LOGOUT");
                    sessionRepository.save(s);
                })
        );
    }

    // (호환용) String 파라미터를 쓰는 호출지 대비
    @Transactional
    public void logout(String userId) { logout(Long.valueOf(userId)); }

    /** 리프레시 만료일이 지났으면 세션 만료(EXPIRED)로 전환 */
    @Transactional
    public void expireIfRefreshPassed(Long userId) {
        userRepository.findById(userId).ifPresent(u ->
                sessionRepository.findWithLockingByUser_Id(userId).ifPresent(s -> {
                    if (s.isActive() && s.getRefreshExpireAt().isBefore(java.time.LocalDateTime.now())) {
                        s.expire();
                        sessionRepository.save(s);
                    }
                })
        );
    }

    // (호환용)
    @Transactional
    public void expireIfRefreshPassed(String userId) { expireIfRefreshPassed(Long.valueOf(userId)); }
}
