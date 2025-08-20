// src/main/java/com/pacs/molecoms/security/JwtAuthFilter.java
package com.pacs.molecoms.security;

import io.jsonwebtoken.Claims;
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
import java.util.stream.Collectors;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) { this.jwtUtil = jwtUtil; }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String auth = req.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                Claims claims = jwtUtil.parseClaims(token);
                String username = claims.getSubject();

                // roles 클레임을 ["ROLE_..."] 또는 ["..."] 둘 다 허용
                Object rolesObj = claims.get("roles");
                List<String> roles = new ArrayList<>();
                if (rolesObj instanceof Collection<?> c) {
                    for (Object o : c) roles.add(String.valueOf(o));
                } else if (rolesObj instanceof String s) {
                    roles = Arrays.stream(s.split(",")).map(String::trim).toList();
                }

                var authorities = roles.stream()
                        .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                var authToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (Exception e) {
                // 토큰이 없거나 잘못되면 그냥 익명으로 진행 → 엔드포인트에서 401/403 처리
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(req, res);
    }
}
