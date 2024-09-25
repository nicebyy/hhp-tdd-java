package io.hhplus.tdd.point.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChargePointRequest {

    @NotNull(message = "충전 포인트는 필수 입력 값입니다.")
    @Positive(message = "충전 포인트는 양수 이어야 합니다.")
    @Min(value = 1,message = "충전 포인트는 1 이상이어야 합니다.")
    long amount;
}
