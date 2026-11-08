package cn.techstar.pvms.backend.module.strategy.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record StrategyBatchRequest(
    @NotEmpty(message = "strategies 不能为空")
    @Size(max = 100, message = "strategies 单次不能超过 100 条")
    List<@NotNull(message = "strategies 不能包含空元素") @Valid StrategyRequest> strategies
) {
}
