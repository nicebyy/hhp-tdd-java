package io.hhplus.tdd.point.controller;

import io.hhplus.tdd.common.ApiResponse;
import io.hhplus.tdd.point.dto.ChargePointRequest;
import io.hhplus.tdd.point.dto.UsePointRequest;
import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.service.PointHistoryService;
import io.hhplus.tdd.point.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/point")
@RequiredArgsConstructor
@Slf4j
public class PointController {

    private final PointService pointService;
    private final PointHistoryService pointHistoryService;

    /**
     * 포인트 조회 API
     */
    @GetMapping("{id}")
    public ResponseEntity<ApiResponse> point(
            @PathVariable long id
    ) {
        UserPoint userPoint = pointService.findUserPointById(id);
        return ResponseEntity.ok(ApiResponse.success(userPoint));
    }

    /**
     * 포인트 히스토리 조회 API
     */
    @GetMapping("{id}/histories")
    public ResponseEntity<ApiResponse> history(
            @PathVariable long id
    ) {
        List<PointHistory> allHistories = pointHistoryService.findAllHistories(id);
        return ResponseEntity.ok(ApiResponse.success(allHistories));
    }

    /**
     * 포인트 충전 API
     */
    @PatchMapping("{id}/charge")
    public ResponseEntity<ApiResponse> charge(
            @PathVariable long id,
            @RequestBody @Validated ChargePointRequest request
    ) {
        UserPoint userPoint = pointService.chargeUserPoint(id, request.getAmount());
        return ResponseEntity.ok(ApiResponse.success(userPoint));
    }

    /**
     * 포인트 사용 API
     */
    @PatchMapping("{id}/use")
    public ResponseEntity<ApiResponse> use(
            @PathVariable long id,
            @RequestBody @Validated UsePointRequest request
    ) {
        UserPoint userPoint = pointService.expendUserPoint(id, request.getAmount());
        return ResponseEntity.ok(ApiResponse.success(userPoint));
    }
}
