package io.hhplus.tdd.point;

import io.hhplus.tdd.common.enums.ResponseCodeEnum;
import io.hhplus.tdd.common.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class PointTransactionValidator {

    static final Long MAX_BALANCE = 100000L;

    public void validateUsePoint(UserPoint userPoint, long amount){

        if(userPoint.point() < amount){
            throw new BusinessException(ResponseCodeEnum.NOT_ENOUGH_POINT);
        }
    }

    public void validateChargePoint(UserPoint userPoint, long amount){

        if(userPoint.point() + amount > MAX_BALANCE){
            throw new BusinessException(ResponseCodeEnum.BALANCE_EXCEED_LIMIT);
        }
    }
}
