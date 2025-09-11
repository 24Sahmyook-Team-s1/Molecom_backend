// src/main/java/com/pacs/molecoms/security/JwtSessionGuardFilter.java
package com.pacs.molecoms.security;

import com.pacs.molecoms.mysql.repository.AuthSessionRepository;
import com.pacs.molecoms.user.service.SessionRotationService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class JwtSessionGuardFilter extends OncePerRequestFilter {

    private final AuthSessionRepository sessionRepo;
    private final SessionRotationService rotationService; // 회전 책임 분리(@Transactional + PESSIMISTIC_WRITE)
    private final JwtUtil jwtUtil;                        // ✅ JwtVerifier 대신 JwtUtil 사용
    private final CookieUtil cookieUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String at = cookieUtil.getTokenFromCookie(req, "accessToken");
        String rt = cookieUtil.getTokenFromCookie(req, "refreshToken");

        // 1) Access 토큰 우선 검증
        if (at != null) {
            try {
                Long   userId    = Long.valueOf(jwtUtil.getSubject(at)); // sub = userId
                String accessJti = jwtUtil.getJti(at);

                var sess = sessionRepo.findByUser_Id(userId).orElse(null);
                if (sess != null && sess.isActive()
                        && accessJti.equals(sess.getAccessJti())
                        && sess.getAccessExpireAt().isAfter(LocalDateTime.now())) {
                    chain.doFilter(req, res);
                    return;
                }
                // access 불일치/만료면 아래 refresh 검사로
            } catch (ExpiredJwtException e) {
                // access 만료 → refresh로 시도
            } catch (JwtException | IllegalArgumentException e) {
                // 위조/형식/subject 파싱 에러 → refresh로 시도
            }
        }

        // 2) Refresh 검증 & 회전(rotate)
        if (rt != null) {
            try {
                Long   userId     = Long.valueOf(jwtUtil.getSubject(rt));
                String refreshJti = jwtUtil.getJti(rt);

                var newAccessOpt = rotationService.rotateIfValid(userId, refreshJti,  res, cookieUtil);
                if (newAccessOpt.isPresent()) {
                    // 새 access/refresh 쿠키까지 교체됨 → 통과
                    chain.doFilter(req, res);
                    return;
                }
            } catch (ExpiredJwtException e) {
                // refresh 자체가 만료 → 아래 공통 실패 처리
            } catch (JwtException | IllegalArgumentException  e) {
                // 위조/형식/subject 파싱 에러
            }
        }

        // 3) 실패: 쿠키 삭제 + 401
        clearAndReject(res);
    }

    private void clearAndReject(HttpServletResponse res) throws IOException {
        cookieUtil.clearJwtCookie(res, "accessToken", false);
        cookieUtil.clearJwtCookie(res, "refreshToken", false);
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"code\":\"UNAUTHORIZED\",\"message\":\"세션이 유효하지 않습니다.\"}");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true; // CORS preflight
        return path.startsWith("/swagger")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/actuator")
                || path.equals("/error")
                || path.equals("/api/users/login")
                || path.equals("/api/users/signup")
                || path.equals("/api/users/logout");
    }
}
