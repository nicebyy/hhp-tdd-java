package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;
    private final PointTransactionValidator pointTransactionValidator;

    public UserPoint findUserPointById(long id){
        UserPoint userPoint = userPointTable.selectById(id);
        return userPoint;
    }

    public UserPoint chargeUserPoint(long id, long amount){
        UserPoint userPoint = findUserPointById(id);
        pointTransactionValidator.validateChargePoint(userPoint,amount);
        return userPointTable.insertOrUpdate(id, amount);
    }

    public UserPoint expendUserPoint(long id, long amount){
        UserPoint userPoint = findUserPointById(id);
        pointTransactionValidator.validateUsePoint(userPoint,amount);
        return userPointTable.insertOrUpdate(id,amount);
    }
}
