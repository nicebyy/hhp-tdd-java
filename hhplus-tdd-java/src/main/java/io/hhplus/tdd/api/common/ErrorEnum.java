package io.hhplus.tdd.api.common;

import lombok.Getter;

@Getter
public enum ErrorEnum {

    NOT_ENOUGH_POINT(-0x1001, "포인트가 부족 합니다."),
    BALANCE_EXCEED_LIMIT(-0x1001, "포인트 충전은 최대한도를 넘을 수 없습니다.(최대 한도: 100000)"),

    ;

    private final int code;
    private final String message;
    ErrorEnum(int code, String message){
        this.code = code;
        this.message = message;
    }
}
