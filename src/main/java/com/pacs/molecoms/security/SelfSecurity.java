package com.pacs.molecoms.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.util.Objects;

@Component("self") // SpEL에서 @self 로 사용
public class SelfSecurity {

    public boolean isSelf(Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return false;

        // 1) 커스텀 Principal 이 id를 들고 있는 경우
        Object p = authentication.getPrincipal();
        if (p instanceof HasUserId u) { // HasUserId는 getId()만 가진 인터페이스 (아래 예시)
            return Objects.equals(u.getId(), id);
        }

        // 2) JWT에 uid 클레임이 있는 경우 (JwtAuthenticationToken을 쓰는 경우)
        try {
            Object uid = authentication.getDetails() instanceof JwtDetails d ? d.uid() : null;
            if (uid != null) return Objects.equals(Long.valueOf(uid.toString()), id);
        } catch (Exception ignore) {}

        // 필요하면 email/subject 비교 등 로직 추가
        return false;
    }

    public interface HasUserId {
        Long getId();
    }

    // (선택) JwtAuthFilter에서 setDetails로 넣어줄 DTO
    public record JwtDetails(Long uid, String email) {}
}
