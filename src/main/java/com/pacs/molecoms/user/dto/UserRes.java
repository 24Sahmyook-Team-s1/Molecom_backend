package com.pacs.molecoms.user.dto;

import com.pacs.molecoms.mysql.entity.UserRole;
import com.pacs.molecoms.mysql.entity.UserStatus;


public record UserRes(
        String email,
        String displayName,
        String dept,
        UserRole role,
        UserStatus status,
        java.time.LocalDateTime createdAt
) {}
