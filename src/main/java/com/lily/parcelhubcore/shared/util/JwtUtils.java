package com.lily.parcelhubcore.shared.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtUtils {

    // 过期时间（毫秒）
    private static final long EXPIRATION = 86400000; // 24小时
    // 密钥（至少256位，放配置文件）
    //todo @Value("${jwt.secret}")
    private static final String SECRET = "Z1TIi6xB1peUANzMEslwn1E+VGI8TVOyXat8phJW6BQ=";
    //  HMAC-SHA256 算法，属于对称加密
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    /**
     * 生成 JWT Token
     */
    public static String generateToken(String userCode) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userCode", userCode);

        return Jwts.builder()
                .claims(claims)
                .subject(userCode)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(KEY)
                .compact();
    }

    /**
     * 解析 Token 获取 Claims
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}