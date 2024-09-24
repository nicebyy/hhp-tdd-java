package io.hhplus.tdd.common;

import io.hhplus.tdd.common.enums.ResponseCodeEnum;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ApiResponse {

    private ResponseBuilder response;
    private Object data;

    @Builder
    public ApiResponse(ResponseBuilder response, Object data) {
        this.response = response;
        this.data = data;
    }

    //data가 없는 success
    public static ApiResponse success() {
        return success(null);
    }

    //data가 있는 success
    public static ApiResponse success(Object data) {
        return process(ResponseCodeEnum.SUCCESS, data);
    }

    //failed
    public static ApiResponse failed(ResponseCodeEnum responseCodeEnum) {
        return process(responseCodeEnum, null);
    }

    public static ApiResponse failed(ResponseCodeEnum responseCodeEnum, Object data) {
        return process(responseCodeEnum, data);
    }

    //return 처리
    public static ApiResponse process(ResponseCodeEnum responseCodeEnum, Object data) {
        return ApiResponse.builder()
                .response(ResponseBuilder.builder()
                        .output(responseCodeEnum.getCode())
                        .result(responseCodeEnum.getMessage())
                        .build())
                .data(data)
                .build();
    }

    //메세지 추가
    public ApiResponse message(String message) {
        this.response.setResult(message);
        return this;
    }
}