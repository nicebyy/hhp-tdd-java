package io.hhplus.tdd.point;

import io.hhplus.tdd.common.enums.ResponseCodeEnum;
import io.hhplus.tdd.point.dto.ChargePointRequest;
import io.hhplus.tdd.point.dto.UsePointRequest;
import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.TransactionType;
import io.hhplus.tdd.point.service.PointHistoryService;
import io.hhplus.tdd.point.service.PointService;
import io.hhplus.tdd.point.service.PointTransactionValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PointControllerE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PointService pointService;

    @Autowired
    private PointHistoryService pointHistoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PointTransactionValidator pointTransactionValidator;

    @Test
    @DisplayName("포인트 충전 및 사용 E2E 테스트")
    void ChargePointAndUse() throws Exception {
        // 초기 데이터 설정
        long userId = 1L;

        // 포인트 조회 - 초기 상태 확인
        mockMvc.perform(get("/point/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.point").value(0L));

        // 포인트 충전 요청
        long chargeAmount = 10000L;
        ChargePointRequest chargeRequest = new ChargePointRequest(chargeAmount);
        String chargeRequestJson = objectMapper.writeValueAsString(chargeRequest);

        mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(chargeRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.output").value(ResponseCodeEnum.SUCCESS.getCode()));

        // 포인트 사용 요청
        long useAmount = 5000L;
        UsePointRequest useRequest = new UsePointRequest(useAmount);
        String useRequestJson = objectMapper.writeValueAsString(useRequest);

        mockMvc.perform(patch("/point/{id}/use", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(useRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.output").value(ResponseCodeEnum.SUCCESS.getCode()));

        // 포인트 조회 - 잔액 확인
        mockMvc.perform(get("/point/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.output").value(ResponseCodeEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.point").value(5000L));

        // 포인트 히스토리 조회 요청
        mockMvc.perform(get("/point/{id}/histories", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.output").value(ResponseCodeEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].type").value(TransactionType.CHARGE.toString()))
                .andExpect(jsonPath("$.data[0].amount").value(chargeAmount))
                .andExpect(jsonPath("$.data[1].type").value(TransactionType.USE.toString()))
                .andExpect(jsonPath("$.data[1].amount").value(useAmount));

        // 서비스 레이어를 통한 데이터 확인
        long currentPoint = pointService.findUserPointById(userId).point();
        assertThat(currentPoint).isEqualTo(5000L);

        List<PointHistory> allHistories = pointHistoryService.findAllHistories(userId);
        assertThat(allHistories).hasSize(2);
    }

    @Test
    @DisplayName("포인트를 초과 사용 시 에러 발생 E2E 테스트")
    void usePointExceedingBalance() throws Exception {
        // given
        long userId = 2L;

        // 포인트 충전
        long chargeAmount = 5000L;
        ChargePointRequest chargeRequest = new ChargePointRequest(chargeAmount);
        String chargeRequestJson = objectMapper.writeValueAsString(chargeRequest);

        mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(chargeRequestJson))
                .andExpect(status().isOk());

        // 잔액보다 많은 포인트 사용 요청
        long useAmount = 10000L;
        UsePointRequest useRequest = new UsePointRequest(useAmount);
        String useRequestJson = objectMapper.writeValueAsString(useRequest);

        mockMvc.perform(patch("/point/{id}/use", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(useRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.response.output").value(ResponseCodeEnum.NOT_ENOUGH_POINT.getCode()))
                .andExpect(jsonPath("$.response.result").value(ResponseCodeEnum.NOT_ENOUGH_POINT.getMessage()));
    }

    @Test
    @DisplayName("포인트 충전 시 최대 한도를 초과하는 경우 E2E 테스트")
    void chargePointExceedingMaxBalance() throws Exception {
        // given
        long userId = 3L;

        // 포인트 충전 - 최대 한도까지 충전
        long maxBalance = PointTransactionValidator.MAX_BALANCE;
        ChargePointRequest chargeRequest = new ChargePointRequest(maxBalance);
        String chargeRequestJson = objectMapper.writeValueAsString(chargeRequest);

        mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(chargeRequestJson))
                .andExpect(status().isOk());

        // 추가 충전 시도 - 최대 한도 초과
        long additionalAmount = 1L;
        ChargePointRequest additionalChargeRequest = new ChargePointRequest(additionalAmount);
        String additionalChargeRequestJson = objectMapper.writeValueAsString(additionalChargeRequest);

        mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(additionalChargeRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.response.output").value(ResponseCodeEnum.BALANCE_EXCEED_LIMIT.getCode()))
                .andExpect(jsonPath("$.response.result").value(ResponseCodeEnum.BALANCE_EXCEED_LIMIT.getMessage()));
    }

}