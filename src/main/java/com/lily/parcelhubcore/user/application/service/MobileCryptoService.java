package com.lily.parcelhubcore.user.application.service;

import jakarta.annotation.Resource;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MobileCryptoService {

    @Resource
    private TextEncryptor textEncryptor;

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
}
