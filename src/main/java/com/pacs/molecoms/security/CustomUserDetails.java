package com.pacs.molecoms.security;

import com.pacs.molecoms.mysql.entity.User;
import com.pacs.molecoms.mysql.entity.UserRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 단일 enum 역할(UserRole)이면 이렇게 정규화
        // 예: ADMIN, RAD, USER -> ROLE_ADMIN, ROLE_RAD, ROLE_USER
        UserRole r = user.getRole();
        String roleName = (r != null) ? r.name() : "USER";
        roleName = roleName.toUpperCase(Locale.ROOT);
        if (!roleName.startsWith("ROLE_")) roleName = "ROLE_" + roleName;
        return List.of(new SimpleGrantedAuthority(roleName));
    }

    @Override
    public String getPassword() {
        return user.getPassword() != null ? user.getPassword() : "";
    }

    // 토큰/조회가 이메일 기반이면 username도 이메일로!
    @Override
    public String getUsername() {
        return user.getEmail(); // ← 기존 displayName 대신 email 권장
    }

    public UserRole getRole() { return user.getRole(); }
    public String getEmail() { return user.getEmail(); }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
