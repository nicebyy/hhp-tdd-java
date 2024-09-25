package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.TransactionType;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.service.PointHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * PointHistoryService 단위 테스트
 * PointHistoryService 외 나머지 컴포넌트들은 mocking 처리
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
class PointHistoryServiceUnitTest {

    @InjectMocks
    private PointHistoryService historyService;

    @Mock
    private PointHistoryTable historyTable;

    private UserPoint userPoint;

    @BeforeEach
    void initUserPoint(){
        long userId = 1L;
        long initBalance = 10000L;
        long time = System.currentTimeMillis();
        this.userPoint = new UserPoint(userId,initBalance,time);
    }

    @Test
    @DisplayName("포인트가 충전 히스토리가 생성된다.")
    void addHistoryWithChanging() {

        // given
        long cursor = 1L;
        long userId = userPoint.id();
        long amount = 10000L;
        TransactionType transactionType = TransactionType.CHARGE;
        long updatedMillis = userPoint.updateMillis();

        PointHistory expectedHistory = new PointHistory(cursor, userId, amount, transactionType, updatedMillis);
        List<PointHistory> historyList = List.of(expectedHistory);

        given(historyTable.insert(userId, amount, transactionType, updatedMillis)).willReturn(expectedHistory); // 미리 만들어둔 history 를 반환 함을 모의
        given(historyTable.selectAllByUserId(userId)).willReturn(historyList); // 미리 만들어둔 history 를 반환 함을 모의

        //when
        PointHistory pointHistory = historyService.addHistory(userPoint, amount, transactionType);

        //then
        then(historyTable).should().insert(userId, amount, transactionType, updatedMillis);
        assertThat(pointHistory.id()).isEqualTo(expectedHistory.id());
        assertThat(pointHistory.userId()).isEqualTo(expectedHistory.userId());
        assertThat(pointHistory.amount()).isEqualTo(expectedHistory.amount());
        assertThat(pointHistory.type()).isEqualTo(expectedHistory.type());
        assertThat(historyTable.selectAllByUserId(userId)).hasSize(1);
    }

    @Test
    @DisplayName("포인트가 사용 히스토리가 생성된다.")
    void addHistoryWithUsing() {

        // given
        long cursor = 1L;
        long userId = userPoint.id();
        long amount = 10000L;
        TransactionType transactionType = TransactionType.USE;
        long updatedMillis = userPoint.updateMillis();

        PointHistory expectedHistory = new PointHistory(cursor, userId, amount, transactionType, updatedMillis);
        List<PointHistory> historyList = List.of(expectedHistory);

        given(historyTable.insert(userId, amount, transactionType, updatedMillis)).willReturn(expectedHistory); // 미리 만들어둔 history 를 반환 함을 모의
        given(historyTable.selectAllByUserId(userId)).willReturn(historyList); // 미리 만들어둔 history 를 반환 함을 모의

        //when
        PointHistory pointHistory = historyService.addHistory(userPoint, amount, transactionType);

        //then
        then(historyTable).should().insert(userId, amount, transactionType, updatedMillis);
        assertThat(pointHistory.id()).isEqualTo(expectedHistory.id());
        assertThat(pointHistory.userId()).isEqualTo(expectedHistory.userId());
        assertThat(pointHistory.amount()).isEqualTo(expectedHistory.amount());
        assertThat(pointHistory.type()).isEqualTo(expectedHistory.type());
        assertThat(historyTable.selectAllByUserId(userId)).hasSize(1);
    }

    @Test
    @DisplayName("포인트 트랜잭션이 없는 유저는 빈 리스트를 반환한다.")
    void findAllHistoriesWithNoTransactions() {

        long userId = userPoint.id();

        // given
        List<PointHistory> historyList = List.of();

        given(historyTable.selectAllByUserId(userId)).willReturn(historyList); // 미리 만들어둔 history 를 반환 함을 모의

        //when
        List<PointHistory> histories = historyService.findAllHistories(userId);

        //then
        then(historyTable).should().selectAllByUserId(userId);
        assertThat(historyTable.selectAllByUserId(userId)).isEmpty();
        assertThat(historyTable.selectAllByUserId(userId)).isEqualTo(histories);
    }
}