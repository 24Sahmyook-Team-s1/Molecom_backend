// src/main/java/com/pacs/molecoms/security/JwtAuthFilter.java
package com.pacs.molecoms.security;

import com.pacs.molecoms.mysql.repository.AuthSessionRepository;
import com.pacs.molecoms.user.service.SessionRotationService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

    /**
     * 단일 세션 강제 + 사일런트 리프레시
     * - Access 검증 실패(만료/불일치) 시 Refresh로 조용히 회전(rotate)
     * - Refresh 만료 시 세션을 EXPIRED로 전환하고 401 + 쿠키 삭제
     */
    @RequiredArgsConstructor
    @Component
    public class JwtAuthFilter extends OncePerRequestFilter {

        private final JwtUtil jwtUtil;
        private final CustomUserDetailsService userDetailsService;
        private final AuthSessionRepository sessionRepo;
        private final CookieUtil cookieUtil;
        private final SessionRotationService rotationService;

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {

            String accessToken  = extractAccessToken(request);
            String refreshToken = extractRefreshToken(request);

            var current = SecurityContextHolder.getContext().getAuthentication();
            boolean needSet = (current == null)
                    || (current instanceof AnonymousAuthenticationToken)
                    || !current.isAuthenticated();

            // 1) Access 우선 검증
            if (needSet && accessToken != null) {
                try {
                    Long userId = parseUserId(jwtUtil.getSubject(accessToken));
                    String accessJti = jwtUtil.getJti(accessToken);

                    var sessOpt = sessionRepo.findByUser_Id(userId);
                    if (sessOpt.isPresent()) {
                        var sess = sessOpt.get();
                        if (sess.isActive()
                                && accessJti.equals(sess.getAccessJti())
                                && sess.getAccessExpireAt().isAfter(LocalDateTime.now())) {
                            setAuthenticationFromToken(accessToken, request);
                            chain.doFilter(request, response);
                            return;
                        }
                    }
                } catch (ExpiredJwtException e) {
                    // access 만료 → refresh로 시도
                } catch (JwtException | IllegalArgumentException e) {
                    // 서명/형식 이상 → refresh로 시도
                }
            }

            // 2) Refresh 검증 / 회전
            if (needSet && refreshToken != null) {
                try {
                    Long userId = parseUserId(jwtUtil.getSubject(refreshToken));
                    String refreshJti = jwtUtil.getJti(refreshToken);

                    var newAccessOpt = rotationService.rotateIfValid(userId, refreshJti, response, cookieUtil);
                    if (newAccessOpt.isPresent()) {
                        setAuthenticationFromToken(newAccessOpt.get(), request);
                        chain.doFilter(request, response);
                        return;
                    } else {
                        // 회전 실패(만료/불일치) → 쿠키 삭제 + 401
                        clearAndReject(response);
                        return;
                    }
                } catch (ExpiredJwtException e) {
                    // 파싱 단계에서 이미 만료 → 쿠키 삭제 + 401
                    clearAndReject(response); return;
                } catch (JwtException | IllegalArgumentException e) {
                    clearAndReject(response); return;
                }
            }

            // 3) 실패
            clearAndReject(response);
        }

        private Long parseUserId(String subject) { return Long.valueOf(subject); }

        private String extractAccessToken(HttpServletRequest request) {
            String bearer = request.getHeader("Authorization");
            if (bearer != null && bearer.startsWith("Bearer ")) return bearer.substring(7);
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("accessToken".equals(cookie.getName())) return cookie.getValue();
                }
            }
            return null;
        }

        private String extractRefreshToken(HttpServletRequest request) {
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("refreshToken".equals(cookie.getName())) return cookie.getValue();
                }
            }
            return null;
        }

        private void setAuthenticationFromToken(String token, HttpServletRequest request) {
            String principalId = jwtUtil.getSubject(token);
            List<String> roles = jwtUtil.getRoles(token);
            var authorities = roles.stream()
                    .map(r -> r.toUpperCase(Locale.ROOT))
                    .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                    .distinct()
                    .map(SimpleGrantedAuthority::new).toList();

            var principalFromJwt = new org.springframework.security.core.userdetails.User(principalId, "", authorities);
            Object principal = principalFromJwt;

            try {
                var userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(principalId);
                var merged = userDetails.getAuthorities().stream()
                        .map(a -> a.getAuthority())
                        .map(s -> s.toUpperCase(Locale.ROOT))
                        .map(s -> s.startsWith("ROLE_") ? s : "ROLE_" + s)
                        .distinct()
                        .map(SimpleGrantedAuthority::new).toList();
                if (!merged.isEmpty()) authorities = merged;
                principal = userDetails;
            } catch (Exception ignore) { }

            var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
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

        @Override protected boolean shouldNotFilterAsyncDispatch() { return false; }
        @Override protected boolean shouldNotFilterErrorDispatch() { return false; }
    }
