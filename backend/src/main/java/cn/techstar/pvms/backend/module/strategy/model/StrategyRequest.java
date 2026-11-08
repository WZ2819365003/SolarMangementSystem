package cn.techstar.pvms.backend.module.strategy.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record StrategyRequest(
    @NotBlank(message = "stationId 不能为空")
    @Size(max = 32, message = "stationId 长度不能超过 32")
    String stationId,

    @NotBlank(message = "type 不能为空")
    @Pattern(
        regexp = "peak-shaving|demand-response|grid-constraint|frequency-regulation",
        message = "type 不是有效的策略类型"
    )
    String type,

    @Size(max = 128, message = "name 长度不能超过 128")
    String name,

    @NotNull(message = "targetPowerKw 不能为空")
    @Positive(message = "targetPowerKw 必须大于 0")
    @DecimalMax(value = "9999999999.99", message = "targetPowerKw 超出范围")
    Double targetPowerKw,

    @NotNull(message = "startTime 不能为空")
    LocalDateTime startTime,

    @NotNull(message = "endTime 不能为空")
    LocalDateTime endTime,

    @Size(max = 255, message = "remark 长度不能超过 255")
    String remark,

    @Size(max = 32, message = "mode 长度不能超过 32")
    String mode
) {

    @AssertTrue(message = "endTime 必须晚于 startTime")
    public boolean isTimeRangeValid() {
        return startTime == null || endTime == null || endTime.isAfter(startTime);
    }
}
