package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.entity.TransactionType;
import io.hhplus.tdd.point.entity.UserPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;
    private final PointTransactionValidator pointTransactionValidator;
    private final PointHistoryService pointHistoryService;
    private final ConcurrentHashMap<Long, Lock> lockMap = new ConcurrentHashMap<>();

    private Lock getLock(long userId){
        return lockMap.computeIfAbsent(userId, id -> new ReentrantLock());
    }

    public UserPoint findUserPointById(long id){
        return userPointTable.selectById(id);
    }

    public UserPoint chargeUserPoint(long id, long amount){

        Lock lock = getLock(id);
        lock.lock();
        try{
            UserPoint userPoint = findUserPointById(id);
            pointTransactionValidator.validateChargePoint(userPoint,amount);
            UserPoint result = userPointTable.insertOrUpdate(id, amount);
            pointHistoryService.addHistory(result, amount, TransactionType.CHARGE);
            return result;
        }finally {
            lock.unlock();
        }
    }

    public UserPoint expendUserPoint(long id, long amount){

        Lock lock = getLock(id);
        lock.lock();
        try{
            UserPoint userPoint = findUserPointById(id);
            pointTransactionValidator.validateUsePoint(userPoint,amount);
            UserPoint result = userPointTable.insertOrUpdate(id, amount);
            pointHistoryService.addHistory(result, amount, TransactionType.USE);
            return result;
        }finally {
            lock.unlock();
        }
    }
}
