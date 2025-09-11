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
import java.security.Key;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private final String secretKeyRaw;
    private Key key;

    // 액세스 15분, 리프레시 7일
    private static final long ACCESS_EXPIRATION_MS  = 1000L * 60 * 15;
    private static final long REFRESH_EXPIRATION_MS = 1000L * 60 * 60 * 24 * 7;

    public long getAccessExpirationMs()  { return ACCESS_EXPIRATION_MS; }
    public long getRefreshExpirationMs() { return REFRESH_EXPIRATION_MS; }

    public JwtUtil(@Value("${jwt.secret}") String secretKeyRaw) {
        this.secretKeyRaw = secretKeyRaw;
        if (this.secretKeyRaw == null || this.secretKeyRaw.isBlank()) {
            throw new IllegalArgumentException("❌ JWT_SECRET 환경변수가 설정되지 않았습니다.");
        }
    }

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKeyRaw.getBytes(StandardCharsets.UTF_8));
        System.out.println("✅ JwtUtil 초기화 완료 (key ready)");
    }

    /** subject = userId(문자열), email/roles는 클레임 */
    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ACCESS_EXPIRATION_MS);

        String userId = String.valueOf(user.getId()); // sub
        String email  = user.getEmail();
        List<String> roles = List.of(user.getRole().name());
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .setId(jti)                    // jti(중요)
                .setSubject(userId)            // 🔸 불변 ID를 sub로
                .claim("email", email)         // 🔸 이메일은 별도 클레임
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** subject = userId(문자열), email 클레임 포함(선택) */
    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + REFRESH_EXPIRATION_MS);

        String userId = String.valueOf(user.getId());
        String email  = user.getEmail();
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .setId(jti)
                .setSubject(userId)
                .claim("email", email)         // 리프레시에도 넣어두면 디버깅/감사 좋음
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ===== 파싱/검증 유틸 =====

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /** sub = userId (문자열) */
    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }

    /** jti(UUID) */
    public String getJti(String token) {
        return parseClaims(token).getId();
    }

    /** exp(Date) */
    public Date getExpiration(String token) {
        return parseClaims(token).getExpiration();
    }

    /** email 클레임 */
    public String getEmail(String token) {
        Object v = parseClaims(token).get("email");
        return v != null ? String.valueOf(v) : null;
    }

    /** roles → List<String> */
    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        Object raw = parseClaims(token).get("roles");
        if (raw instanceof Collection<?> c) return c.stream().map(String::valueOf).collect(Collectors.toList());
        if (raw instanceof String s) {
            return Arrays.stream(s.split(",")).map(String::trim).filter(v -> !v.isEmpty()).collect(Collectors.toList());
        }
        return List.of();
    }

    /** 유효성 (만료/서명 등) */
    public boolean validate(String token) {
        try { parseClaims(token); return true; }
        catch (JwtException | IllegalArgumentException e) { return false; }
    }

    /** Authorization 헤더 또는 accessToken 쿠키 */
    public String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) return bearer.substring(7);
        if (request.getCookies() != null) {
            for (var c : request.getCookies()) if ("accessToken".equals(c.getName())) return c.getValue();
        }
        return null;
    }

    /** 남은 만료시간(ms) */
    public long msUntilExpiration(String token) {
        try {
            long expMs = getExpiration(token).toInstant().toEpochMilli();
            long nowMs = Instant.now().toEpochMilli();
            return Math.max(0L, expMs - nowMs);
        } catch (Exception e) { return 0L; }
    }
}
