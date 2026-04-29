package com.lily.parcelhubcore.parcel.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lily.parcelhubcore.parcel.api.request.InboundRequest;
import com.lily.parcelhubcore.parcel.api.request.PrepareInRequest;
import com.lily.parcelhubcore.parcel.application.command.ParcelInBoundCommand;
import com.lily.parcelhubcore.parcel.application.command.PrepareInCommand;
import com.lily.parcelhubcore.parcel.application.dto.PrepareInDTO;
import com.lily.parcelhubcore.parcel.application.service.ParcelOpService;
import com.lily.parcelhubcore.parcel.common.enums.ErrorCode;
import com.lily.parcelhubcore.shared.authentication.dto.LoginUser;
import com.lily.parcelhubcore.shared.enums.OperateTypeEnum;
import com.lily.parcelhubcore.shared.exception.BusinessException;
import com.lily.parcelhubcore.shared.filter.JwtAuthenticationTokenFilter;
import com.lily.parcelhubcore.user.infrastructure.persistence.entity.UserInfoDO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * AutoConfigureMockMvc(addFilters = false)不负责阻止 Filter Bean 创建；
 * 所以还需要手动排出JwtAuthenticationTokenFilter.class，不然还是会创建且因为依赖没有报错
 */
@WebMvcTest(
        controllers = ParcelOpController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationTokenFilter.class
        )
)
// 让 MockMvc 请求不经过 Spring Security 的过滤器链；不把 application context 里的 filters 注册到 MockMvc
@AutoConfigureMockMvc(addFilters = false)
class ParcelOpControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ParcelOpService parcelOpService;

    /**
     * 业务代码从SecurityContextHolder取用户信息，所以需要手动构建
     */
    @BeforeEach
    void setUp() {
        UserInfoDO user = new UserInfoDO();
        user.setCode("U00000111");
        user.setUsername("lily");
        user.setPassword("123456");
        user.setStationCode("ST00000123");
        LoginUser loginUser = new LoginUser(user, List.of("MANAGER"));

        var authenticationToken = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
        // 存入SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void prepareIn_shouldReturnSuccess_whenValidRequest() throws Exception {
        // given
        PrepareInRequest request = new PrepareInRequest();
        request.setWaybillCode("WB123");
        request.setShelfCode("1-1");
        request.setRecipientMobile("13800138000");
        PrepareInDTO dto = new PrepareInDTO();
        dto.setPickupCode("PICK123");

        when(parcelOpService.prepareIn(any(PrepareInCommand.class))).thenReturn(dto);

        // when & then
        mockMvc.perform(post("/parcel/prepare/in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.pickupCode").value("PICK123"));

        verify(parcelOpService).prepareIn(any(PrepareInCommand.class));
    }

    @Test
    void prepareIn_shouldReturnParamInvalid_whenInvalidRequest() throws Exception {
        // given
        String invalidJson = "{}";

        // when & then
        mockMvc.perform(post("/parcel/prepare/in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("PARAM_INVALID"));

        verifyNoInteractions(parcelOpService);
    }

    @Test
    void prepareIn_shouldReturnBusinessError_whenServiceThrowsException() throws Exception {
        // given
        PrepareInRequest request = new PrepareInRequest();
        request.setWaybillCode("WB123");
        request.setShelfCode("1-1");
        request.setRecipientMobile("13800138000");

        when(parcelOpService.prepareIn(any(PrepareInCommand.class))).thenThrow(new BusinessException(ErrorCode.CURRENT_EXCEPTION));

        // when & then
        mockMvc.perform(post("/parcel/prepare/in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("LOCK_FAILED"));

        verify(parcelOpService).prepareIn(any(PrepareInCommand.class));
    }

    @Test
    void inbound_shouldReturnSuccess_whenValidRequest() throws Exception {
        // given
        InboundRequest request = new InboundRequest();
        request.setWaybillCode("WB123");
        request.setShelfCode("1-1");
        request.setPickupCode("1-1-001");
        request.setRecipientMobile("13800138000");

        // when & then
        mockMvc.perform(post("/parcel/inbound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.result").value(true));

        verify(parcelOpService).inbound(any(ParcelInBoundCommand.class));
    }

    @Test
    void inbound_shouldReturnParamInvalid_whenInvalidRequest() throws Exception {
        // given
        String invalidJson = "{}";

        // when & then
        mockMvc.perform(post("/parcel/inbound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("PARAM_INVALID"));

        verifyNoInteractions(parcelOpService);
    }

    @Test
    void inbound_shouldReturnBusinessError_whenServiceThrowsException() throws Exception {
        // given
        InboundRequest request = new InboundRequest();
        request.setWaybillCode("WB123");
        request.setShelfCode("1-1");
        request.setPickupCode("1-1-01");
        request.setRecipientMobile("13800138000");

        doThrow(new BusinessException(ErrorCode.CURRENT_EXCEPTION)).when(parcelOpService).inbound(any(ParcelInBoundCommand.class));

        // when & then
        mockMvc.perform(post("/parcel/inbound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("LOCK_FAILED"));

        verify(parcelOpService).inbound(any(ParcelInBoundCommand.class));
    }

    @Test
    void outbound_shouldReturnSuccess_whenValidRequest() throws Exception {
        // given
        String waybillCode = "WB123";

        // when & then
        mockMvc.perform(post("/parcel/outbound")
                        .param("waybillCode", waybillCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.result").value(true));

        verify(parcelOpService).outBoundOrReturn(waybillCode, OperateTypeEnum.OUT);
    }

    @Test
    void outbound_shouldReturnBusinessError_whenServiceThrowsException() throws Exception {
        // given
        String waybillCode = "WB123";

        doThrow(new BusinessException(ErrorCode.CURRENT_EXCEPTION)).when(parcelOpService).outBoundOrReturn(waybillCode, OperateTypeEnum.OUT);

        // when & then
        mockMvc.perform(post("/parcel/outbound")
                        .param("waybillCode", waybillCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("LOCK_FAILED"));

        verify(parcelOpService).outBoundOrReturn(waybillCode, OperateTypeEnum.OUT);
    }

    @Test
    void outbound_shouldReturnParamInvalid_whenInvalidRequest() throws Exception {
        // when & then
        mockMvc.perform(post("/parcel/outbound")
                        .param("waybillCode", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("PARAM_INVALID"));
    }

    @Test
    void returned_shouldReturnSuccess_whenValidRequest() throws Exception {
        // given
        String waybillCode = "WB123";

        // when & then
        mockMvc.perform(post("/parcel/returned")
                        .param("waybillCode", waybillCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.result").value(true));

        verify(parcelOpService).outBoundOrReturn(waybillCode, OperateTypeEnum.RETURN);
    }

    @Test
    void returned_shouldReturnBusinessError_whenServiceThrowsException() throws Exception {
        // given
        String waybillCode = "WB123";

        doThrow(new BusinessException(ErrorCode.CURRENT_EXCEPTION)).when(parcelOpService).outBoundOrReturn(waybillCode, OperateTypeEnum.RETURN);

        // when & then
        mockMvc.perform(post("/parcel/returned")
                        .param("waybillCode", waybillCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("LOCK_FAILED"));

        verify(parcelOpService).outBoundOrReturn(waybillCode, OperateTypeEnum.RETURN);
    }

    @Test
    void returned_shouldReturnParamInvalid_whenInvalidRequest() throws Exception {
        // when & then
        mockMvc.perform(post("/parcel/returned")
                        .param("waybillCode", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("PARAM_INVALID"));
    }

    @Test
    void transfer_shouldReturnSuccess_whenValidRequest() throws Exception {
        // given
        String waybillCode = "WB123";
        String shelfCode = "1-2";

        // when & then
        mockMvc.perform(post("/parcel/transfer")
                        .param("waybillCode", waybillCode)
                        .param("shelfCode", shelfCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.result").value(true));

        verify(parcelOpService).transfer(waybillCode, shelfCode);
    }

    @Test
    void transfer_shouldReturnBusinessError_whenServiceThrowsException() throws Exception {
        // given
        String waybillCode = "WB123";
        String shelfCode = "1-2";

        doThrow(new BusinessException(ErrorCode.CURRENT_EXCEPTION)).when(parcelOpService).transfer(waybillCode, shelfCode);

        // when & then
        mockMvc.perform(post("/parcel/transfer")
                        .param("waybillCode", waybillCode)
                        .param("shelfCode", shelfCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("LOCK_FAILED"));

        verify(parcelOpService).transfer(waybillCode, shelfCode);
    }

    @Test
    void transfer_shouldReturnParamInvalid_whenInvalidRequest() throws Exception {

        // when & then
        mockMvc.perform(post("/parcel/transfer")
                        .param("waybillCode", "")
                        .param("shelfCode", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("PARAM_INVALID"));
    }

    /**
     * 无法注定注入ObjectMapper，手动new
     */
    @TestConfiguration
    static class TestConfig {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper().findAndRegisterModules();
        }
    }

}