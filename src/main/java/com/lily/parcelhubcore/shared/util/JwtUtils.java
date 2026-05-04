package com.lily.parcelhubcore.shared.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

    // 过期时间（毫秒）
    private static final long EXPIRATION = 86400000; // 24小时

    @Value("${jwt.secret}")
    private String secret;

    /**
     * 生成 JWT Token
     */
    public String generateToken(String userCode) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userCode", userCode);

        return Jwts.builder()
                .claims(claims)
                .subject(userCode)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析 Token 获取 Claims
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        //  HMAC-SHA256 算法，属于对称加密
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
