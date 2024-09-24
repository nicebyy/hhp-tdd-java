package io.hhplus.tdd.common.exception;

import io.hhplus.tdd.common.enums.ResponseCodeEnum;

public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(ResponseCodeEnum code) {
        super(code.getMessage());
    }

    public BusinessException(ResponseCodeEnum code, String message) {
        super(message);
    }
}
