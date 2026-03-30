package cn.techstar.pvms.backend.module.strategy.model;

import java.time.LocalDateTime;

public record StrategyRequest(
    String stationId,
    String type,
    String name,
    Double targetPowerKw,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String remark,
    String mode
) {
}
