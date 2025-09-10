package com.pacs.molecoms.security.jwt;

import java.time.LocalDateTime;

public record IssuedTokens(
        String accessToken, String refreshToken,
        String accessJti, String refreshJti,
        LocalDateTime accessExpiresAt, LocalDateTime refreshExpiresAt
) {}
