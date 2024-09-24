package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;
    private final PointTransactionValidator pointTransactionValidator;
    private final PointHistoryService pointHistoryService;

    public UserPoint findUserPointById(long id){
        UserPoint userPoint = userPointTable.selectById(id);
        return userPoint;
    }

    public UserPoint chargeUserPoint(long id, long amount){
        UserPoint userPoint = findUserPointById(id);
        pointTransactionValidator.validateChargePoint(userPoint,amount);
        UserPoint result = userPointTable.insertOrUpdate(id, amount);
        pointHistoryService.addHistory(result, amount, TransactionType.CHARGE);
        return result;
    }

    public UserPoint expendUserPoint(long id, long amount){
        UserPoint userPoint = findUserPointById(id);
        pointTransactionValidator.validateUsePoint(userPoint,amount);
        UserPoint result = userPointTable.insertOrUpdate(id, amount);
        pointHistoryService.addHistory(result, amount, TransactionType.USE);
        return result;
    }
}
