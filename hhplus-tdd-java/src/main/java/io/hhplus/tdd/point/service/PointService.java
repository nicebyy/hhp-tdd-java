package io.hhplus.tdd.point.service;

import io.hhplus.tdd.common.exception.BusinessException;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.entity.TransactionType;
import io.hhplus.tdd.point.entity.UserPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;
    private final PointTransactionValidator pointTransactionValidator;
    private final PointHistoryService pointHistoryService;
    private final ConcurrentHashMap<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    private ReentrantLock getLock(long userId){
        return lockMap.computeIfAbsent(userId, id -> new ReentrantLock());
    }

    public UserPoint findUserPointById(long id){
        return userPointTable.selectById(id);
    }

    public UserPoint chargeUserPoint(long id, long amount){

        ReentrantLock lock = getLock(id);
        lock.lock();
        try{
            UserPoint userPoint = findUserPointById(id);
            pointTransactionValidator.validateChargePoint(userPoint,amount);
            UserPoint result = userPointTable.insertOrUpdate(id, amount+userPoint.point());
            pointHistoryService.addHistory(result, amount, TransactionType.CHARGE);
            return result;
        }finally {
            lock.unlock();
        }
    }

    public UserPoint expendUserPoint(long id, long amount){

        ReentrantLock lock = getLock(id);
        lock.lock();
        try{
            UserPoint userPoint = findUserPointById(id);
            pointTransactionValidator.validateUsePoint(userPoint,amount);
            UserPoint result = userPointTable.insertOrUpdate(id, userPoint.point()-amount);
            pointHistoryService.addHistory(result, amount, TransactionType.USE);
            return result;
        } finally {
            lock.unlock();
        }
    }
}
