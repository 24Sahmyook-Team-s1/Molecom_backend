// src/main/java/com/pacs/molecoms/security/JwtAuthFilter.java
package com.pacs.molecoms.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

@RequiredArgsConstructor
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = null;

        // 1) Authorization 헤더
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            token = bearerToken.substring(7);
        }
        // 2) 쿠키
        if (token == null && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        System.out.println("\n\n🛡️ JwtAuthenticationFilter 진입");
        System.out.println("🛡️ 요청 URI: " + request.getRequestURI());
        System.out.println("🛡️ 추출된 토큰: " + token + "\n");

        var current = SecurityContextHolder.getContext().getAuthentication();
        boolean needSet = (current == null)
                || (current instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)
                || !current.isAuthenticated();

        if (token != null && jwtUtil.validateToken(token) && needSet) {
            try {
                String userId = jwtUtil.getUserIdFromToken(token); // e.g., email/uid
                System.out.println("🛡️ 사용자 ID: " + userId);

                // roles 클레임 파싱
                Object rawRoles = jwtUtil.getClaim(token, "roles");
                List<String> roles;
                if (rawRoles instanceof Collection<?> c) {
                    roles = c.stream().map(String::valueOf).toList();
                } else if (rawRoles instanceof String s) {
                    roles = Arrays.stream(s.split(","))
                            .map(String::trim)
                            .filter(v -> !v.isEmpty())
                            .toList();
                } else {
                    roles = List.of();
                }

                // 권한 정규화
                List<SimpleGrantedAuthority> authorities =
                        roles.isEmpty()
                                ? List.of() // 비어 있어도 null은 금지
                                : roles.stream()
                                .map(r -> r.toUpperCase(Locale.ROOT))
                                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                                .map(SimpleGrantedAuthority::new)
                                .toList();

                // 1차: JWT만으로 principal 구성 (DB 실패해도 인증 세우기)
                org.springframework.security.core.userdetails.User principalFromJwt =
                        new org.springframework.security.core.userdetails.User(userId, "", authorities);

                // 2차: 가능하면 DB로 보강(실패해도 무시)
                Object principal = principalFromJwt;
                try {
                    CustomUserDetails userDetails =
                            (CustomUserDetails) userDetailsService.loadUserByUsername(userId);
                    // DB 권한도 ROLE_ 접두사 보정
                    var mergedAuth = userDetails.getAuthorities().stream()
                            .map(a -> a.getAuthority())
                            .map(s -> s.toUpperCase(Locale.ROOT))
                            .map(s -> s.startsWith("ROLE_") ? s : "ROLE_" + s)
                            .distinct()
                            .map(SimpleGrantedAuthority::new)
                            .toList();
                    principal = userDetails;
                    // DB 권한이 있으면 교체, 없으면 JWT 권한 유지
                    if (!mergedAuth.isEmpty()) {
                        authorities = mergedAuth;
                    }
                } catch (Exception ignore) { /* 보강 실패는 OK */ }

                // Authentication 생성/주입 (권한 컬렉션은 null 금지)
                var authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                System.out.println("🛡️ 부여 권한: " + authorities);

            } catch (Exception e) {
                System.out.println("❌ JWT 인증 처리 실패: " + e.getMessage());
                e.printStackTrace();
            }
        }

        filterChain.doFilter(request, response);
    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.startsWith("/swagger")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.equals("/api/users/login")
                || path.equals("/api/users/signup")
                || path.equals("/api/users/logout");  // ✅ "/api/auth/me" 절대 금지!
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        // 기본값(true) → async 디스패치 시 필터를 건너뜀
        // false로 바꿔서 async 디스패치에도 JWT 재적용
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        // 에러 디스패치에도 JWT 적용(에러 핸들러 경로 접근 시 컨텍스트 보존)
        return false;
    }

}
