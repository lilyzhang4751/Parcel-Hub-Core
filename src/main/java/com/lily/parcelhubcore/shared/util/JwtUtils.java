package com.lily.parcelhubcore.shared.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


public class JwtUtils {

    // 过期时间（毫秒）
    private static final long EXPIRATION = 86400000; // 24小时
    // 密钥（至少256位，放配置文件）
    //@Value("${jwt.secret}")
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
        try {
            return Jwts.parser()
                    .verifyWith(KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token已过期");
        } catch (UnsupportedJwtException e) {
            throw new RuntimeException("Token格式错误");
        } catch (MalformedJwtException e) {
            throw new RuntimeException("Token解析失败");
        } catch (SecurityException e) {
            throw new RuntimeException("Token签名验证失败");
        }
    }

    /**
     * 验证 Token 是否有效
     */
    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从 Token 获取用户ID
     */
    public static String getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userCode", String.class);
    }

    /**
     * 判断 Token 是否过期
     */
    public static boolean isExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }
}