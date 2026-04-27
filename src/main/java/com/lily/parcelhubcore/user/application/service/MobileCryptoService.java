package com.lily.parcelhubcore.user.application.service;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MobileCryptoService {

    @Resource
    private TextEncryptor textEncryptor;

    @Value("${app.crypto.hash.secret}")
    private String mobileHashSecret;

    /**
     * 手机号加密后存库
     */
    public String encryptMobile(String rawMobile) {
        if (!StringUtils.hasText(rawMobile)) {
            return rawMobile;
        }
        return textEncryptor.encrypt(rawMobile.trim());
    }

    /**
     * 从数据库取出后解密
     */
    public String decryptMobile(String encryptedMobile) {
        if (!StringUtils.hasText(encryptedMobile)) {
            return encryptedMobile;
        }
        return textEncryptor.decrypt(encryptedMobile);
    }

    /**
     * 手机号hash
     */
    public String hash(String rowMobile) {
        if (rowMobile == null || rowMobile.isBlank()) {
            return null;
        }

        try {
            var mac = Mac.getInstance("HmacSHA256");
            var keySpec = new SecretKeySpec(getHashSecret(), "HmacSHA256");
            mac.init(keySpec);

            var digest = mac.doFinal(rowMobile.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to hash mobile", e);
        }
    }

    private byte[] getHashSecret() {
        return mobileHashSecret.getBytes(StandardCharsets.UTF_8);
    }
}
