package cn.techstar.pvms.backend.module.strategy.model;

import java.util.List;

public record StrategyBatchRequest(List<StrategyRequest> strategies) {
}
