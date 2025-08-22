// src/main/java/com/pacs/molecoms/security/JwtAuthFilter.java
package com.pacs.molecoms.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

        // ✅ 1. Authorization 헤더에서 꺼내기
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            token = bearerToken.substring(7);
        }

        // ✅ 2. 없으면 쿠키에서 accessToken 꺼내기
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

        // ✅ 3. 토큰 검증 및 사용자 인증 설정
        if (token != null && jwtUtil.validateToken(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String userId = jwtUtil.getUserIdFromToken(token); // email:provider
                System.out.println("🛡️ 사용자 ID: " + userId);

                CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(userId);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                System.out.println("❌ JWT 인증 처리 실패: " + e.getMessage());
                e.printStackTrace();  // ✅ 오류 추적 로그
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
                || path.startsWith("/oauth2/")
                || path.startsWith("/login/oauth2/")
                || path.equals("/api/auth/login")
                || path.equals("/api/auth/signup")
                || path.equals("/api/auth/logout");  // ✅ "/api/auth/me" 절대 금지!
    }
}
