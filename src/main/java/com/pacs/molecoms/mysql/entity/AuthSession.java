package com.pacs.molecoms.mysql.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(
        name = "auth_session",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_auth_session_user", columnNames = "user_id") // 유저당 1행
        },
        indexes = {
                @Index(name = "idx_auth_session_session_id", columnList = "session_id"),
                @Index(name = "idx_auth_session_access_jti", columnList = "access_jti"),
                @Index(name = "idx_auth_session_refresh_jti", columnList = "refresh_jti"),
                @Index(name = "idx_auth_session_active", columnList = "active")
        }
)
public class AuthSession {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 낙관적 락(선택): 동시에 회전/로그인 갱신 충돌 탐지
    @Version
    private Long version;

    // 유저당 1행: 단일 활성 세션
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 서버 세션 식별자(로그/감사/디바이스 구분)
    @Column(name = "session_id", nullable = false, length = 36)
    private String sessionId;

    // 액세스/리프레시 토큰의 jti(원문 토큰은 저장하지 않음)
    @Column(name = "access_jti", nullable = false, length = 36)
    private String accessJti;

    @Column(name = "refresh_jti", nullable = false, length = 36)
    private String refreshJti;

    // 만료 시각(서버 보조 검증)
    @Column(name = "access_expire_at", nullable = false)
    private LocalDateTime accessExpireAt;

    @Column(name = "refresh_expire_at", nullable = false)
    private LocalDateTime refreshExpireAt;

    // 세션 상태
    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "revoked_reason", length = 32)
    private String revokedReason; // TAKEN_OVER / LOGOUT / EXPIRED 등

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /* ---------- 팩토리 & 편의 메서드 ---------- */

    // 활성 세션 생성
    public static AuthSession newActive(
            User user, String accessJti, String refreshJti,
            LocalDateTime accessExp, LocalDateTime refreshExp
    ) {
        return AuthSession.builder()
                .user(user)
                .sessionId(UUID.randomUUID().toString())
                .accessJti(accessJti)
                .refreshJti(refreshJti)
                .accessExpireAt(accessExp)
                .refreshExpireAt(refreshExp)
                .active(true)
                .build();
    }

    // 새 로그인으로 기존 단일 세션 가로채기 시, sessionId 재발급
    public void takeOverWithNewSessionId() {
        this.sessionId = UUID.randomUUID().toString();
        this.active = true;
        this.revokedAt = null;
        this.revokedReason = null;
    }

    public void expire() {
        this.active = false;
        this.revokedAt = LocalDateTime.now();
        this.revokedReason = "EXPIRED";
    }

    public void revoke(String reason) {
        this.active = false;
        this.revokedAt = LocalDateTime.now();
        this.revokedReason = reason;
    }

    public void rotate(String newAccessJti, String newRefreshJti,
                       LocalDateTime newAccessExp, LocalDateTime newRefreshExp) {
        this.accessJti = newAccessJti;
        this.refreshJti = newRefreshJti;
        this.accessExpireAt = newAccessExp;
        this.refreshExpireAt = newRefreshExp;
        // sessionId는 회전 시 유지(동일 세션)
    }

    /* ---------- 안전장치: NULL 삽입 방지 ---------- */

    @PrePersist
    public void prePersist() {
        // 세션ID가 비어있으면 자동 생성 (session_id NOT NULL 보호)
        if (this.sessionId == null || this.sessionId.isBlank()) {
            this.sessionId = UUID.randomUUID().toString();
        }
        // 필수 플래그 기본값
        // active가 null로 들어오는 케이스 방지 (boolean이므로 기본 false지만 명시)
        this.active = (this.active);
    }

    @PreUpdate
    public void preUpdate() {
        // 회전/업데이트 중 필수값 누락 방지
        if (this.sessionId == null || this.sessionId.isBlank()) {
            this.sessionId = UUID.randomUUID().toString();
        }
    }
}
