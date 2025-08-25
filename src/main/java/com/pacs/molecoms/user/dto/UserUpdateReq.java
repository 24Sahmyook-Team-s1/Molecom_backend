package com.pacs.molecoms.user.dto;

import com.pacs.molecoms.mysql.entity.UserRole;
import com.pacs.molecoms.mysql.entity.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserUpdateReq(
        @NotBlank String displayName,
        String dept,
        @NotNull UserRole role,
        @NotNull UserStatus status
) {}
