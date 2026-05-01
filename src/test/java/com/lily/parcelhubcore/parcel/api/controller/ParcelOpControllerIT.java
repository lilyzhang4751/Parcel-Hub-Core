package com.lily.parcelhubcore.parcel.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.lily.parcelhubcore.parcel.api.request.InboundRequest;
import com.lily.parcelhubcore.shared.authentication.dto.LoginUser;
import com.lily.parcelhubcore.shared.enums.WaybillRegistryStatusEnum;
import com.lily.parcelhubcore.user.infrastructure.persistence.entity.UserInfoDO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(
        statements = {
                "TRUNCATE TABLE parcel_op_record RESTART IDENTITY CASCADE",
                "TRUNCATE TABLE message_outbox RESTART IDENTITY CASCADE",
                "TRUNCATE TABLE parcel RESTART IDENTITY CASCADE",
                "TRUNCATE TABLE waybill_registry RESTART IDENTITY CASCADE"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
public class ParcelOpControllerIT extends AbstractIntegrationTest {

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

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanRedis() {
        stringRedisTemplate
                .getConnectionFactory()
                .getConnection()
                .serverCommands()
                .flushDb();
    }

    @Test
    void shouldInboundParcelSuccessfully_whenValidRequest() throws Exception {
        var request = buildInboundRequest();
        var testStationCode = "ST00000001";
        var testWaybillCode = request.getWaybillCode();

        // when: 执行 HTTP 请求
        mockMvc.perform(post("/parcel/inbound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(authentication(authenticationToken)))
                // then: 校验 HTTP 响应
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.result").value(true));

        // then: 数据库断言
        // 1. 验证 parcel 表是否已插入
        Integer parcelCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM parcel WHERE waybill_code = ? AND station_code = ?",
                Integer.class, testWaybillCode, testStationCode
        );
        assertThat(parcelCount).isEqualTo(1);

        // 2. 验证 parcel 表的具体字段
        String pickupCodeInDb = jdbcTemplate.queryForObject(
                "SELECT pickup_code FROM parcel WHERE waybill_code = ? AND station_code = ? LIMIT 1",
                String.class, testWaybillCode, testStationCode
        );
        assertThat(pickupCodeInDb).isEqualTo(request.getPickupCode());

        String shelfCodeInDb = jdbcTemplate.queryForObject(
                "SELECT shelf_code FROM parcel WHERE waybill_code = ? AND station_code = ? LIMIT 1",
                String.class, testWaybillCode, testStationCode
        );
        assertThat(shelfCodeInDb).isEqualTo(request.getShelfCode());

        // 3. 验证 parcel_op_record 表是否有新记录（操作类型应为 IN=100）
        Integer opRecordCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM parcel_op_record WHERE waybill_code = ? AND station_code = ? AND op_type = 100",
                Integer.class, testWaybillCode, testStationCode
        );
        assertThat(opRecordCount).isEqualTo(1);

        // 4. 验证 message_outbox 表是否有 NEW 状态的消息
        Integer messageCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM message_outbox WHERE status = 'NEW'",
                Integer.class
        );
        assertThat(messageCount).isGreaterThanOrEqualTo(1);

        // 5. 验证 waybill_registry 状态
        Integer waybillStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM waybill_registry WHERE waybill_code = ?",
                Integer.class, testWaybillCode
        );
        assertThat(waybillStatus).isEqualTo(WaybillRegistryStatusEnum.OCCUPIED.getCode());
    }

    @Test
    void shouldInboundParcelFailed_whenValidRequestAndWaybillRegistryExists() throws Exception {
        var request = buildInboundRequest();
        var testStationCode = "ST00000001";
        var testWaybillCode = request.getWaybillCode();
        jdbcTemplate.update("INSERT INTO waybill_registry (waybill_code, station_code, status) VALUES (?, ?, ?)",
                testWaybillCode, testStationCode, WaybillRegistryStatusEnum.OCCUPIED.getCode());

        // when: 执行 HTTP 请求
        mockMvc.perform(post("/parcel/inbound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(authentication(authenticationToken)))
                // then: 校验 HTTP 响应
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("PARCEL_ALREADY_EXIST"));
    }

    /**
     * 构建入库请求
     */
    private InboundRequest buildInboundRequest() {
        // 构建请求
        var request = new InboundRequest();
        request.setWaybillCode("WB202604290001");
        request.setShelfCode("1-2");
        request.setPickupCode("1-2-001");
        request.setRecipientName("张三");
        request.setRecipientMobile("13800138000");
        return request;
    }
}
