package io.hhplus.tdd.point;

import io.hhplus.tdd.point.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *  PointTransactionValidator 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class PointTransactionValidatorUnitTest {

    @InjectMocks
    private PointTransactionValidator validator;

    @Test
    @DisplayName("사용 하려는 포인트가 가진 포인트보다 적으면 예외를 발생 시킨다.")
    void checkUserPointWithNotEnoughPoint() {

        //given
        long userId = 1L;
        long chargeAmount = 100L;
        long currentTimeMillis = System.currentTimeMillis();
        UserPoint userPoint = new UserPoint(userId, chargeAmount, currentTimeMillis);

        long expendAmount = 200L;

        //when then
        assertThrows(
                BusinessException.class,
                ()->validator.validateUsePoint(userPoint,expendAmount)
        );
    }

    @Test
    @DisplayName("사용 하려는 포인트가 가진 포인트보다 많으면 정상동작한다.")
    void checkUserPointWithEnoughPoint() {

        //given
        long userId = 1L;
        long chargeAmount = 200L;
        long currentTimeMillis = System.currentTimeMillis();
        UserPoint userPoint = new UserPoint(userId, chargeAmount, currentTimeMillis);

        long expendAmount = 200L;

        //when then
        assertDoesNotThrow(()->validator.validateUsePoint(userPoint,expendAmount));
    }

    @Test
    @DisplayName("충전 이후 포인트 합이 최대한도를 초과하면 예외를 발생시킨다.")
    void checkUserPointWithExceedingMaxLimit() {

        //given
        long userId = 1L;
        long chargeAmount = 20000000L;
        UserPoint userPoint = UserPoint.empty(userId);

        //when then
        assertThrows(
                BusinessException.class,
                ()->validator.validateChargePoint(userPoint,chargeAmount)
        );
    }

    @Test
    @DisplayName("충전 이후 포인트 합이 최대한도를 넘지 않는다.")
    void checkUserPointWithNotExceedingMaxLimit() {

        //given
        long userId = 1L;
        long chargeAmount = 20000L;
        UserPoint userPoint = UserPoint.empty(userId);

        //when then
        assertDoesNotThrow(()->validator.validateChargePoint(userPoint,chargeAmount));
    }
}