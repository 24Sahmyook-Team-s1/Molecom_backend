// src/main/java/com/pacs/molecoms/security/JwtUtil.java
package com.pacs.molecoms.security;

import com.pacs.molecoms.mysql.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.security.Key;

@Component
public class JwtUtil {

    private final String secretKeyRaw;
    private Key key;

    // 15분
    private static final long ACCESS_EXPIRATION = 1000L * 60 * 15;
    // 7일
    private static final long REFRESH_EXPIRATION = 1000L * 60 * 60 * 24 * 7;

    public long getACCESS_EXPIRATION() {
        return ACCESS_EXPIRATION;
    }

    public JwtUtil(@Value("${jwt.secret}") String secretKeyRaw) {
        this.secretKeyRaw = secretKeyRaw;
        if (this.secretKeyRaw == null || this.secretKeyRaw.isBlank()) {
            throw new IllegalArgumentException("❌ JWT_SECRET 환경변수가 설정되지 않았습니다.");
        }
    }

    @PostConstruct
    public void init() {
        System.out.println("🔑 로드된 secretKeyRaw 길이: " + secretKeyRaw.length());
        this.key = Keys.hmacShaKeyFor(secretKeyRaw.getBytes(StandardCharsets.UTF_8));
        System.out.println("✅ JwtUtil 초기화 완료 (key ready)");
    }

    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ACCESS_EXPIRATION);

        String email = user.getEmail();
        String role = user.getRole().name();

        // subject는 email만 넣습니다(일관성)
        // roles는 배열 클레임로 저장
        List<String> roles = List.of(role);

        System.out.println("🔐 accessToken 생성 → subject(email): " + email + ", roles: " + roles);

        return Jwts.builder()
                .setSubject(email)
                .claim("uid", user.getId())
                .claim("roles", roles) // ← 필터가 사용하는 표준 키
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + REFRESH_EXPIRATION);

        String email = user.getEmail();

        System.out.println("🔐 refreshToken 생성 → subject(email): " + email);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** subject(email) 반환 */
    public String getUserIdFromToken(String token) {
        try {
            String subject = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();

            System.out.println("🔎 getUserIdFromToken → subject(email): " + subject);
            return subject;

        } catch (Exception e) {
            System.out.println("❌ getUserIdFromToken 예외: " + e.getMessage());
            throw new IllegalArgumentException("토큰 파싱에 실패했습니다: " + e.getMessage());
        }
    }

    /** 임의 클레임 조회용(필터에서 roles 가져갈 때 사용) */
    public Object getClaim(String token, String keyName) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get(keyName);
        } catch (Exception e) {
            System.out.println("❌ getClaim 예외(" + keyName + "): " + e.getMessage());
            return null;
        }
    }

    /** roles 클레임을 List<String> 로 반환 (없으면 빈 리스트) */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Object raw = getClaim(token, "roles");
        if (raw instanceof Collection<?> c) {
            return c.stream().map(String::valueOf).collect(Collectors.toList());
        } else if (raw instanceof String s) {
            return Arrays.stream(s.split(","))
                    .map(String::trim)
                    .filter(v -> !v.isEmpty())
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            System.out.println("✅ 토큰 유효성 검증 성공");
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("⏰ 만료된 토큰입니다: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.out.println("🚫 지원하지 않는 토큰입니다: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println("❌ 잘못된 형식의 토큰입니다: " + e.getMessage());
        } catch (SignatureException e) {
            System.out.println("🔐 서명이 올바르지 않습니다: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("⚠️ 잘못된 요청입니다: " + e.getMessage());
        }

        debugToken(token); // 실패 시 디버깅 로그 추가
        return false;
    }

    public String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public void debugToken(String token) {
        System.out.println("🧪 디버그용 토큰 분석 시작");
        try {
            var parsed = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            System.out.println("✅ 디버그: payload = " + parsed.getBody());
        } catch (Exception e) {
            System.out.println("❌ 디버그 실패: " + e.getMessage());
        }
    }
}
