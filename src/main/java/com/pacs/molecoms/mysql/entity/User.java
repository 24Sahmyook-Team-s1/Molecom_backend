package com.pacs.molecoms.mysql.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
        indexes = @Index(name = "idx_users_dept", columnList = "dept")
)
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=255)
    private String email;

    @Column(name="display_name", nullable=false, length=100)
    private String displayName;

    @Column
    private String password;

    @Column
    private String provider;

    @Column(length=100)
    private String dept;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private UserStatus status;

    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private LocalDateTime createdAt;
}
