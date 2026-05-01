package com.lily.parcelhubcore.parcel.api.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.lily.parcelhubcore.parcel.api.request.PrepareInRequest;
import com.lily.parcelhubcore.shared.authentication.dto.LoginUser;
import com.lily.parcelhubcore.user.infrastructure.persistence.entity.UserInfoDO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.ObjectMapper;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AuthenticationIT {

    private static Authentication authenticationToken;

    static {
        // 构建测试用户并设置 authentication
        UserInfoDO user = new UserInfoDO();
        user.setCode("U00000001");
        user.setUsername("testOperator");
        user.setPassword("123456");
        user.setStationCode("ST00000001");
        LoginUser loginUser = new LoginUser(user, List.of("STAFF"));
        authenticationToken = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
    }

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16-alpine");

    @Container
    @ServiceConnection
    static GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379);

    @Container
    @ServiceConnection
    static KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void givenUnauthenticated_whenAccessProtectedEndpoint_thenReturn401() throws Exception {
        mockMvc.perform(post("/parcel/prepare/in")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(minimalRequestJson()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void givenStaffRole_whenAccessManagerEndpoint_thenReturn403() throws Exception {
        mockMvc.perform(get("/notify/sms/{waybillCode}", "WB202604290001")
                        .with(authentication(authenticationToken)))
                .andExpect(jsonPath("$.code").value("403"));
    }

    @Test
    void givenStaffRole_whenAccessStaffEndpoint_thenPassSecurityCheck() throws Exception {
        mockMvc.perform(get("/parcels/{waybillCode}", "WB202604290001")
                        .with(csrf())
                        .with(authentication(authenticationToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("PARCEL_NOT_EXIST"));
    }

    private String minimalRequestJson() throws Exception {
        return objectMapper.writeValueAsString(new PrepareInRequest("WB202604290001", "4-29", "lilyTest", "13800000000"));
    }

}
