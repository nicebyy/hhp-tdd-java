package io.hhplus.tdd.point;

import io.hhplus.tdd.common.enums.ResponseCodeEnum;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.common.exception.BusinessException;
import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.TransactionType;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.service.PointHistoryService;
import io.hhplus.tdd.point.service.PointService;
import io.hhplus.tdd.point.service.PointTransactionValidator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

/**
 * PointService 단위테스트
 * PointService 외 다른 컴포넌트는 전부 mocking 처리 하였음.
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
class PointServiceUnitTest {

    @InjectMocks
    private PointService pointService;

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointTransactionValidator validator;

    @Mock
    private PointHistoryService pointHistoryService;

    @Test
    @DisplayName("새로운 유저가 포인트를 충전한다.")
    void chargeUserPointByNewUser() {

        // given
        long userId = 1L;
        long chargeAmount = 100L;
        long currentTimeMillis = System.currentTimeMillis();
        UserPoint userPoint = UserPoint.empty(userId); // 조회로 반환 될 empty userPoint
        UserPoint expectedUserPoint = new UserPoint(userId, chargeAmount, currentTimeMillis); // 충전 후 반환 될 userPoint
        PointHistory expectedHistory = new PointHistory(1L, userId, chargeAmount, TransactionType.CHARGE, currentTimeMillis);

        given(userPointTable.selectById(userId)).willReturn(userPoint);
        willDoNothing().given(validator).validateChargePoint(userPoint, chargeAmount); // 포인트 충전시 예외 던지지 않음을 모의
        given(userPointTable.insertOrUpdate(userId, chargeAmount)).willReturn(expectedUserPoint); // 미리 만들어 둔 expectedUserPoint 를 반환 함을 모의
        given(pointHistoryService.addHistory(expectedUserPoint, chargeAmount, TransactionType.CHARGE)).willReturn(expectedHistory); // 미리 만들어 둔 history 객체를 반환 함을 모의

        // when
        UserPoint result = pointService.chargeUserPoint(userId, chargeAmount);

        // then
        then(userPointTable).should().selectById(userId);
        then(userPointTable).should().insertOrUpdate(userId, chargeAmount);  // db insert 메서드 호출 검증
        then(validator).should().validateChargePoint(userPoint, chargeAmount); // validator 호출 검증
        then(pointHistoryService).should().addHistory(result, chargeAmount, TransactionType.CHARGE); // addHistory 호출 검증
        assertThat(result).isEqualTo(expectedUserPoint);  // 결과 검증
        assertThat(result.point()).isEqualTo(userPoint.point() + chargeAmount);  // 결과 검증
    }

    @Test
    @DisplayName("기존 유저가 포인트를 충전한다.")
    void chargeUserPointByExistingUser() {

        // given
        long userId = 1L;
        long chargeAmount1 = 100L;
        long chargeAmount2 = 200L;
        UserPoint userPoint = pointService.chargeUserPoint(userId, chargeAmount1);
        UserPoint afterChargeUserPoint = new UserPoint(userId, chargeAmount1 + chargeAmount2, System.currentTimeMillis()); // 충전 후 반환 될 userPoint
        PointHistory expectedHistory = new PointHistory(2L, userId, chargeAmount2, TransactionType.CHARGE, afterChargeUserPoint.updateMillis()); // 충전 후 기록될 history 모의 객체 (2번 충전 이므로 cursor 는 2L)

        given(userPointTable.insertOrUpdate(userId, chargeAmount2)).willReturn(afterChargeUserPoint);
        willDoNothing().given(validator).validateChargePoint(userPoint, chargeAmount2); // 포인트 충전시 예외 던지지 않음을 모의
        given(pointHistoryService.addHistory(afterChargeUserPoint, chargeAmount2, TransactionType.CHARGE)).willReturn(expectedHistory); // 미리 만들어 둔 history 객체를 반환 함을 모의

        // when
        UserPoint result = pointService.chargeUserPoint(userId, chargeAmount2);

        // then
        verify(userPointTable, times(2)).selectById(userId);
        then(userPointTable).should().insertOrUpdate(userId, chargeAmount2);
        then(validator).should().validateChargePoint(userPoint, chargeAmount2);
        then(pointHistoryService).should().addHistory(result, chargeAmount2, TransactionType.CHARGE); // addHistory 호출 검증
        assertThat(result).isEqualTo(afterChargeUserPoint);
    }

    @Test
    @DisplayName("충전한 적 없는 유저의 포인트를 조회하면 빈 유저 포인트 객체를 반환 한다.")
    void findUserPointWithNoCharging() {

        // given (사전 조건 설정)
        long userId = 1L;
        UserPoint expectedUserPoint = UserPoint.empty(userId);
        given(userPointTable.selectById(userId)).willReturn(expectedUserPoint);

        // when
        UserPoint result = pointService.findUserPointById(userId);

        // then
        then(userPointTable).should().selectById(userId);// 메서드 호출 검증
        assertThat(result).isEqualTo(expectedUserPoint); // 결과 검증
    }

    @Test
    @DisplayName("충전한 포인트 보다 적거나 같게 사용한다.")
    void usePointWithEnough() {

        // given
        long userId = 1L;
        long chargeAmount = 100L;
        long currentTimeMillis = System.currentTimeMillis();
        UserPoint userPoint = new UserPoint(userId, chargeAmount, currentTimeMillis);

        long expendAmount = 50L;
        UserPoint expectedUserPoint = new UserPoint(userId, chargeAmount - expendAmount, currentTimeMillis); // 포인트를 사용 후 반환 될 userPoint 모의 객체

        PointHistory expectedHistory = new PointHistory(2L, userId, expendAmount, TransactionType.USE, currentTimeMillis); // 포인트를 사용 후 반환 될 history 모의 객체

        given(userPointTable.selectById(userId)).willReturn(userPoint); // 포인트 조회 동작
        willDoNothing().given(validator).validateUsePoint(userPoint, expendAmount); // 포인트 사용 검증
        given(userPointTable.insertOrUpdate(userId, expendAmount)).willReturn(expectedUserPoint); // 포인트 사용 시도
        given(pointHistoryService.addHistory(expectedUserPoint, expendAmount, TransactionType.USE)).willReturn(expectedHistory); // 미리 만들어 둔 history 객체를 반환 함을 모의

        // when
        UserPoint result = pointService.expendUserPoint(userId, expendAmount);

        // then
        then(userPointTable).should().selectById(userId); // 기존 userPoint 조회 검증
        then(validator).should().validateUsePoint(userPoint, expendAmount);  // validator 호출 검증
        then(userPointTable).should().insertOrUpdate(userId, chargeAmount - expendAmount);  // 포인트 업데이트 호출 검증
        then(pointHistoryService).should().addHistory(result, expendAmount, TransactionType.USE); // addHistory 호출 검증
        assertThat(result).isEqualTo(expectedUserPoint);  // 결과 검증
    }

    @Test
    @DisplayName("포인트가 충분하지 않아 예외가 발생한다.")
    void usePointWithNotEnoughShouldThrowException() {

        // given
        long userId = 1L;
        long chargeAmount = 100L;
        long expendAmount = 200L;
        long currentTimeMillis = System.currentTimeMillis();

        UserPoint userPoint = new UserPoint(userId, chargeAmount, currentTimeMillis);

        given(userPointTable.selectById(userId)).willReturn(userPoint);

        // 포인트 부족 시 예외 던지기 모의
        willThrow(new BusinessException(ResponseCodeEnum.NOT_ENOUGH_POINT))
                .given(validator)
                .validateUsePoint(userPoint, expendAmount);

        // when & then
        org.junit.jupiter.api.Assertions
                .assertThrows(
                        BusinessException.class,
                        () -> pointService.expendUserPoint(userId, expendAmount)
                );

        then(userPointTable).should().selectById(userId); // 기존 userPoint 조회 검증
        then(validator).should().validateUsePoint(userPoint, expendAmount);
        then(userPointTable).should(never()).insertOrUpdate(anyLong(), anyLong());  // 포인트가 부족하여 insertOrUpdate는 호출되지 않았음을 검증
        then(pointHistoryService).should(never()).addHistory(any(),anyLong(),any()); // 예외 발생으로 인하여 history 가 쌓이지 않았음을 검증
    }

    @Test
    @DisplayName("포인트 최대 잔고 넘어선 충전으로 예외가 발생한다.")
    void chargePointExceedingMaxLimitShouldThrowException() {

        // given
        long userId = 1L;
        long chargeAmount = 100000000L;
        UserPoint userPoint = UserPoint.empty(userId);

        given(userPointTable.selectById(userId)).willReturn(userPoint);

        // 포인트 부족 시 예외 던지기 모의
        willThrow(new BusinessException(ResponseCodeEnum.BALANCE_EXCEED_LIMIT))
                .given(validator)
                .validateChargePoint(userPoint, chargeAmount);

        // when & then
        org.junit.jupiter.api.Assertions
                .assertThrows(
                        BusinessException.class,
                        () -> pointService.chargeUserPoint(userId, chargeAmount)
                );

        then(userPointTable).should().selectById(userId); // 기존 userPoint 조회 검증
        then(validator).should().validateChargePoint(userPoint, chargeAmount);
        then(userPointTable).should(never()).insertOrUpdate(anyLong(), anyLong());  // 최대 잔고를 초과하여 insertOrUpdate는 호출되지 않았음을 검증
        then(pointHistoryService).should(never()).addHistory(any(),anyLong(),any()); // 예외 발생으로 인하여 history 가 쌓이지 않았음을 검증
    }
}
