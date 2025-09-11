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

        boolean triedRefresh = false; // 마지막에 판단용

        // 1) Access 우선
        if (at != null) {
            try {
                Long userId = Long.valueOf(jwtUtil.getSubject(at));
                String accessJti = jwtUtil.getJti(at);

                var sess = sessionRepo.findByUser_Id(userId).orElse(null);
                if (sess != null && sess.isActive()
                        && accessJti.equals(sess.getAccessJti())
                        && sess.getAccessExpireAt().isAfter(LocalDateTime.now())) {
                    chain.doFilter(req, res);
                    return;
                }
                // access 불일치/만료 → refresh 검사로 진행
            } catch (ExpiredJwtException ignore) {
                // access 문제 → refresh 검사로
            } catch (JwtException ignore) {
                // access 문제 → refresh 검사로
            } catch (IllegalArgumentException ignore) {
                // access 문제 → refresh 검사로
            }
        }

        // 2) Refresh 검사 & 회전
        if (rt != null) {
            triedRefresh = true;
            try {
                Long userId = Long.valueOf(jwtUtil.getSubject(rt));
                String refreshJti = jwtUtil.getJti(rt);

                // ❗ 인자 순서 바로잡기: (userId, refreshJti, cookieUtil, res)
                var newAccessOpt = rotationService.rotateIfValid(userId, refreshJti, cookieUtil, res);
                if (newAccessOpt.isPresent()) {
                    chain.doFilter(req, res);
                    return;
                }
                // 회전 실패 → 아래 공통 처리
            } catch (ExpiredJwtException ignore) {
                // access 문제 → refresh 검사로
            } catch (JwtException ignore) {
                // access 문제 → refresh 검사로
            } catch (IllegalArgumentException ignore) {
                // access 문제 → refresh 검사로
            }
        }

        // 3) 실패 처리
        if (triedRefresh) {
            // refresh가 있었지만 실패(없음/무효/만료/세션불일치) → 둘 다 삭제
            clearAndReject(res, true);
        } else {
            // refresh 자체가 없음 → access만 삭제 (refresh는 건드릴 게 없음)
            clearAndReject(res, false);
        }
    }

    private void clearAndReject(HttpServletResponse res, boolean clearRefreshToo) throws IOException {
        cookieUtil.clearJwtCookie(res, "accessToken", false);
        if (clearRefreshToo) {
            cookieUtil.clearJwtCookie(res, "refreshToken", false);
        }
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
                || path.equals("/api/users/signup");
    }

    @Override protected boolean shouldNotFilterAsyncDispatch() { return false; }
    @Override protected boolean shouldNotFilterErrorDispatch() { return false; }
}
