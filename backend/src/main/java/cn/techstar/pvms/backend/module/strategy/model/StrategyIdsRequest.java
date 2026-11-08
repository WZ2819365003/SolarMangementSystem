package cn.techstar.pvms.backend.module.strategy.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record StrategyIdsRequest(
    @NotEmpty(message = "ids 不能为空")
    @Size(max = 500, message = "ids 单次不能超过 500 条")
    List<@NotBlank(message = "ids 不能包含空值") @Size(max = 32, message = "id 长度不能超过 32") String> ids
) {
}
