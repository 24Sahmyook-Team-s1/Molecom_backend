package com.pacs.molecoms.security.jwt;

import io.jsonwebtoken.Claims;

public interface JwtVerifier {
    Claims verify(String accessToken);         // 액세스
    Claims verifyRefresh(String refreshToken); // 리프레시
}
