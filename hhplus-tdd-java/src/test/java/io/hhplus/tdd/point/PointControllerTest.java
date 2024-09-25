package io.hhplus.tdd.point;

import io.hhplus.tdd.common.ApiResponse;
import io.hhplus.tdd.common.enums.ResponseCodeEnum;
import io.hhplus.tdd.common.exception.BusinessException;
import io.hhplus.tdd.point.controller.PointController;
import io.hhplus.tdd.point.dto.ChargePointRequest;
import io.hhplus.tdd.point.dto.UsePointRequest;
import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.TransactionType;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.service.PointHistoryService;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PointController.class)
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointService pointService;

    @MockBean
    private PointHistoryService pointHistoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("포인트 조회 API 테스트")
    void findPointById() throws Exception {
        // given
        long userId = 1L;
        UserPoint userPoint = new UserPoint(userId, 10000L, System.currentTimeMillis());

        given(pointService.findUserPointById(userId)).willReturn(userPoint);

        // when & then
        mockMvc.perform(get("/point/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.output").value(ResponseCodeEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.response.result").value(ResponseCodeEnum.SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.point").value(10000L));
    }

    @Test
    @DisplayName("포인트 히스토리 조회 API 테스트")
    void findPointHistory() throws Exception {
        // given
        long userId = 1L;
        List<PointHistory> histories = Arrays.asList(
                new PointHistory(1L, userId, 5000L, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2L, userId, 2000L, TransactionType.USE, System.currentTimeMillis())
        );

        given(pointHistoryService.findAllHistories(userId)).willReturn(histories);

        // when & then
        mockMvc.perform(get("/point/{id}/histories", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.output").value(ResponseCodeEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.response.result").value(ResponseCodeEnum.SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].userId").value(userId))
                .andExpect(jsonPath("$.data[0].amount").value(5000L))
                .andExpect(jsonPath("$.data[0].type").value(TransactionType.CHARGE.toString()))
                .andExpect(jsonPath("$.data[0].updateMillis").exists())
                .andExpect(jsonPath("$.data[1].id").value(2L))
                .andExpect(jsonPath("$.data[1].userId").value(userId))
                .andExpect(jsonPath("$.data[1].amount").value(2000L))
                .andExpect(jsonPath("$.data[1].type").value(TransactionType.USE.toString()))
                .andExpect(jsonPath("$.data[1].updateMillis").exists());
    }

    @Test
    @DisplayName("포인트 충전 API 테스트")
    void chargePoint() throws Exception {
        // given
        long userId = 1L;
        long amount = 5000L;
        ChargePointRequest request = new ChargePointRequest(amount);

        UserPoint updatedUserPoint = new UserPoint(userId, 15000L, System.currentTimeMillis());

        given(pointService.chargeUserPoint(userId, amount)).willReturn(updatedUserPoint);

        // when & then
        mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.output").value(ResponseCodeEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.response.result").value(ResponseCodeEnum.SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.point").value(15000L));
    }

    @Test
    @DisplayName("포인트 사용 API 테스트")
    void usePoint() throws Exception {
        // given
        long userId = 1L;
        long amount = 2000L;
        UsePointRequest request = new UsePointRequest(amount);

        UserPoint updatedUserPoint = new UserPoint(userId, 8000L, System.currentTimeMillis());

        given(pointService.expendUserPoint(userId, amount)).willReturn(updatedUserPoint);

        // when & then
        mockMvc.perform(patch("/point/{id}/use", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.output").value(ResponseCodeEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.response.result").value(ResponseCodeEnum.SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.point").value(8000L));
    }

    @Test
    @DisplayName("포인트 사용 시 잔액 부족으로 실패하는 경우 테스트")
    void usePointInsufficientBalance() throws Exception {
        // given
        long userId = 1L;
        long amount = 20000L; // 잔액보다 큰 금액
        UsePointRequest request = new UsePointRequest(amount);

        given(pointService.expendUserPoint(userId, amount))
                .willThrow(new BusinessException(ResponseCodeEnum.NOT_ENOUGH_POINT));

        // when & then
        mockMvc.perform(patch("/point/{id}/use", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.response.output").value(ResponseCodeEnum.NOT_ENOUGH_POINT.getCode()))
                .andExpect(jsonPath("$.response.result").value(ResponseCodeEnum.NOT_ENOUGH_POINT.getMessage()))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("포인트 충전 시 금액이 음수이면 요청 거부")
    void chargePointWithNegativeAmount() throws Exception {
        // given
        long userId = 1L;
        long amount = -5000L; // 음수 금액
        ChargePointRequest request = new ChargePointRequest(amount);

        // when & then
        mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.response.output").value(ResponseCodeEnum.VALIDATION_ERROR.getCode()))
                .andExpect(jsonPath("$.response.result").value(ResponseCodeEnum.VALIDATION_ERROR.getMessage()))
                .andExpect(jsonPath("$.data").isMap());
    }
}