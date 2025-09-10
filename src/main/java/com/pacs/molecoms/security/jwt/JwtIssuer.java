package com.pacs.molecoms.security.jwt;

public interface JwtIssuer {
    IssuedTokens issue(String userId); // sub=userId, jti 각각 UUID
}
