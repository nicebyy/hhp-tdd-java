package io.hhplus.tdd.point.exception;

import io.hhplus.tdd.api.common.ErrorEnum;

public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(ErrorEnum code) {
        super(code.getMessage());
    }

    public BusinessException(ErrorEnum code, String message) {
        super(message);
    }
}
