package com.lily.parcelhubcore;

import java.util.Base64;
import javax.crypto.SecretKey;

import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.WaybillRegistryRepository;
import com.lily.parcelhubcore.shared.util.JwtUtils;
import com.lily.parcelhubcore.user.application.service.LoginService;
import com.lily.parcelhubcore.user.infrastructure.persistence.repository.UserInfoRepository;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class ParcelHubCoreApplicationTests {

    @Resource
    private WaybillRegistryRepository waybillRegistryRepository;

    @Resource
    private UserInfoRepository userInfoRepository;

    @Resource
    private LoginService loginService;

    @Test
    void contextLoads() {
        var user = userInfoRepository.findByUserName("lily");
        System.out.println(user);

        var encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("123456"));
    }

    @Test
    void login_test(){
        // 生成安全的随机密钥
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

        // 转为 Base64 字符串（存配置文件用）
        String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
        System.out.println("Base64密钥: " + base64Key);

        loginService.login("lily","123456");
    }

    @Test
    void token_test(){
        var token = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyQ29kZSI6Im5vMDAwMSIsInN1YiI6Im5vMDAwMSIsImlhdCI6MTc3NjE1MTEzMSwiZXhwIjoxNzc2MjM3NTMxfQ.tXSRzjxMzsU2G89z7JBL5DN4EdXQ3Iv4F8aKXjCO30k";
        var result = JwtUtils.parseToken(token);
        System.out.println(result);
    }

}
