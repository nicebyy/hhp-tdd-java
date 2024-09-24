package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointHistoryService {

    private final PointHistoryTable historyTable;

    public PointHistory addHistory(UserPoint userPoint, long amount, TransactionType transactionType){
        return this.historyTable.insert(userPoint.id(),amount ,transactionType, userPoint.updateMillis());
    }

    public List<PointHistory> findAllHistories(long userId){
        return this.historyTable.selectAllByUserId(userId);
    }
}
