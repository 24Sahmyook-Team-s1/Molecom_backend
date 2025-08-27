package com.pacs.molecoms.mysql.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(
        name = "auth_session"//,
//        uniqueConstraints = @UniqueConstraint(name = "uk_session_id", columnNames = "id"),
//        indexes = @Index(name = "idx_users_dept", columnList = "dept")
)
public class Session {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "jwt_token")
    private String accessToken;

    @Column
    private Date issued_at;

    @Column
    private Date expires_at;
}
