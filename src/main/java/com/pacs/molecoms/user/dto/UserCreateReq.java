package com.pacs.molecoms.user.dto;

import com.pacs.molecoms.mysql.entity.UserRole;
import com.pacs.molecoms.mysql.entity.UserStatus;
import jakarta.validation.constraints.*;

public record UserCreateReq(
        @Email @NotBlank String email,
        @NotBlank String displayName,
        String dept,
        @NotNull UserRole role,
        @NotNull UserStatus status,
        @NotBlank String passWord
) {}

