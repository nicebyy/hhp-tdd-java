package io.hhplus.tdd.common.enums;

import lombok.Getter;

@Getter
public enum ResponseCodeEnum {

    SUCCESS(0x0000, "success"),
    FAILED(-0x0001, "failed"),
    ERROR(-0x0002,"error"),

    NOT_ENOUGH_POINT(-0x1001, "포인트가 부족 합니다."),
    BALANCE_EXCEED_LIMIT(-0x1002, "포인트 충전은 최대한도를 넘을 수 없습니다.(최대 한도: 100000)"),

    ;
    private final int code;

    private final String message;

    ResponseCodeEnum(int code, String message){
        this.code = code;
        this.message = message;
    }
}