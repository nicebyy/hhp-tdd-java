package io.hhplus.tdd.point;

import io.hhplus.tdd.common.exception.BusinessException;
import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.TransactionType;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.service.PointHistoryService;
import io.hhplus.tdd.point.service.PointService;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@SpringBootTest
class PointServiceIntegrationTest {

    @Autowired
    PointService pointService;

    @Autowired
    PointHistoryService pointHistoryService;

    @Test
    @DisplayName("새로운 유저 한명이 포인트를 한번 충전한다.")
    void chargeUserPointByNewUser(){

        //given
        long userId = 1L;
        long chargeAmount = 100L;

        //when
        UserPoint userPoint = pointService.chargeUserPoint(userId, chargeAmount);
        List<PointHistory> histories = pointHistoryService.findAllHistories(userPoint.id());
        UserPoint findedUserPoint = pointService.findUserPointById(userPoint.id());

        //then
        assertThat(userPoint).isEqualTo(findedUserPoint).isNotNull();
        assertThat(histories).hasSize(1);
    }

    @Test
    @DisplayName("포인트를 여러번 충전 한다.")
    void chargeMultiple(){

        //given
        long userId = 2L;
        List<Long> chargeAmountList = List.of(100L,200L,300L);

        //when
        chargeAmountList.forEach(amount -> {
            UserPoint userPoint = pointService.chargeUserPoint(userId, amount);
        });

        //then
        List<PointHistory> histories = pointHistoryService.findAllHistories(userId);
        UserPoint findedUserPoint = pointService.findUserPointById(userId);

        assertThat(userId).isEqualTo(findedUserPoint.id());
        assertThat(chargeAmountList.stream().reduce(0L,Long::sum)).isEqualTo(findedUserPoint.point());
        assertThat(histories).hasSize(chargeAmountList.size());
    }

    @Test
    @DisplayName("포인트를 충전 후 사용한다.")
    void chargeAndUsePoint(){

        //given
        long userId = 3L;
        long currentAmount = 1000L;
        long useAmount = 200L;

        //when
        pointService.chargeUserPoint(userId, currentAmount);
        pointService.expendUserPoint(userId, useAmount);
        UserPoint findedUserPoint = pointService.findUserPointById(userId);
        List<PointHistory> histories = pointHistoryService.findAllHistories(userId);

        //then
        assertThat(userId).isEqualTo(findedUserPoint.id());
        assertThat(findedUserPoint.point()).isEqualTo(currentAmount - useAmount);
        assertThat(histories).hasSize(2);
        assertThat(histories.get(0).type()).isEqualTo(TransactionType.CHARGE);
        assertThat(histories.get(1).type()).isEqualTo(TransactionType.USE);
    }

    @Test
    @DisplayName("충전할때 포인트 합이 최대한도를 초과하면 예외를 발생시킨다.")
    void chargePointOverMaxLimit(){

        //given
        long userId = 4L;
        long currentAmount = 1000L;
        long chargeAmount = 20000000000L;

        //when
        UserPoint userPoint = pointService.chargeUserPoint(userId, currentAmount);

        //then
        assertThrows(
                BusinessException.class,
                ()->pointService.chargeUserPoint(userId, chargeAmount)
        );
        assertThat(userId).isEqualTo(userPoint.id());
        assertThat(pointHistoryService.findAllHistories(userId)).hasSize(1);
        assertThat(pointService.findUserPointById(userId).point()).isEqualTo(userPoint.point());
    }

    @Test
    @DisplayName("사용할때 가진 포인트보다 더 많이 사용하면 예외를 발생시킨다.")
    void usePointOverCurrentPoint(){

        //given
        long userId = 5L;
        long currentAmount = 1000L;
        long useAmount = 20000000000L;

        //when
        UserPoint userPoint = pointService.chargeUserPoint(userId, currentAmount);

        //then
        assertThrows(
                BusinessException.class,
                ()->pointService.expendUserPoint(userId, useAmount)
        );
        assertThat(userId).isEqualTo(userPoint.id());
        assertThat(pointHistoryService.findAllHistories(userId)).hasSize(1);
        assertThat(pointService.findUserPointById(userId).point()).isEqualTo(userPoint.point());
    }

    @Test
    @DisplayName("충전한 적 없는 유저의 포인트를 조회하면 빈 유저 포인트 객체를 반환 한다.")
    void findUserPointWithNoCharging(){

        //given
        long userId = 6L;

        //when
        UserPoint userPoint = pointService.findUserPointById(userId);
        List<PointHistory> histories = pointHistoryService.findAllHistories(userPoint.id());

        //then
        assertEquals(userPoint,UserPoint.empty(userId));
        assertThat(histories).isEmpty();
    }

    @Test
    @DisplayName("포인트를 동시에 여러번 충전한다.")
    void chargeWithConcurrencyBySingleUser() throws InterruptedException {

        // given
        long userId = 7L;
        int numberOfThreads = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(20);

        // when
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> {
                pointService.chargeUserPoint(userId, 1000L);
                latch.countDown();
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        UserPoint userPoint = pointService.findUserPointById(userId);
        assertEquals(1000L*100, userPoint.point());
    }
}
