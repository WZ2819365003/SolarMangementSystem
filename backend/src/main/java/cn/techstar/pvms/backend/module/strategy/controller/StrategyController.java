package cn.techstar.pvms.backend.module.strategy.controller;

import cn.techstar.pvms.backend.common.ApiResponse;
import cn.techstar.pvms.backend.module.strategy.model.StrategyBatchRequest;
import cn.techstar.pvms.backend.module.strategy.model.StrategyIdsRequest;
import cn.techstar.pvms.backend.module.strategy.model.StrategyRequest;
import cn.techstar.pvms.backend.module.strategy.service.StrategyDataService;
import cn.techstar.pvms.backend.module.strategy.service.StrategyRevenueService;
import cn.techstar.pvms.backend.module.strategy.service.StrategySimulationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pvms/strategy")
public class StrategyController {

    private final StrategyDataService strategyDataService;
    private final StrategySimulationService strategySimulationService;
    private final StrategyRevenueService strategyRevenueService;

    public StrategyController(
        StrategyDataService strategyDataService,
        StrategySimulationService strategySimulationService,
        StrategyRevenueService strategyRevenueService
    ) {
        this.strategyDataService = strategyDataService;
        this.strategySimulationService = strategySimulationService;
        this.strategyRevenueService = strategyRevenueService;
    }

    @GetMapping("/meta")
    public ApiResponse<Map<String, Object>> getMeta() {
        return ApiResponse.success(strategyDataService.getMeta());
    }

    @GetMapping("/tree")
    public ApiResponse<Map<String, Object>> getTree(@RequestParam(required = false) String resourceUnitId) {
        return ApiResponse.success(strategyDataService.getTree(resourceUnitId));
    }

    @GetMapping("/kpi")
    public ApiResponse<Map<String, Object>> getKpi(
        @RequestParam(required = false) String resourceUnitId,
        @RequestParam(required = false) String stationId
    ) {
        return ApiResponse.success(strategyDataService.getKpi(resourceUnitId, stationId));
    }

    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> getList(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) String region,
        @RequestParam(required = false) String resourceUnitId,
        @RequestParam(required = false) String stationId,
        @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(strategyDataService.getList(status, type, region, resourceUnitId, stationId, keyword));
    }

    @GetMapping("/detail")
    public ApiResponse<Map<String, Object>> getDetail(@RequestParam String id) {
        return ApiResponse.success(strategyDataService.getDetail(id));
    }

    @GetMapping("/electricity-price")
    public ApiResponse<Map<String, Object>> getElectricityPrice(@RequestParam(required = false) String stationId) {
        return ApiResponse.success(strategyDataService.getElectricityPrice(stationId));
    }

    @PostMapping("/create")
    public ApiResponse<Map<String, Object>> create(@RequestBody StrategyRequest request) {
        return ApiResponse.success(strategyDataService.createStrategy(request));
    }

    @PostMapping("/batch-create")
    public ApiResponse<Map<String, Object>> batchCreate(@RequestBody StrategyBatchRequest request) {
        return ApiResponse.success(strategyDataService.batchCreateStrategies(request == null ? List.of() : request.strategies()));
    }

    @PostMapping("/submit")
    public ApiResponse<Map<String, Object>> submit(@RequestParam String id) {
        return ApiResponse.success(strategyDataService.submitStrategy(id));
    }

    @PostMapping("/terminate")
    public ApiResponse<Map<String, Object>> terminate(@RequestParam String id) {
        return ApiResponse.success(strategyDataService.terminateStrategy(id));
    }

    @PostMapping("/batch-submit")
    public ApiResponse<Map<String, Object>> batchSubmit(@RequestBody StrategyIdsRequest request) {
        return ApiResponse.success(strategyDataService.batchSubmit(request == null ? List.of() : request.ids()));
    }

    @PostMapping("/batch-delete")
    public ApiResponse<Map<String, Object>> batchDelete(@RequestBody StrategyIdsRequest request) {
        return ApiResponse.success(strategyDataService.batchDelete(request == null ? List.of() : request.ids()));
    }

    @PostMapping("/simulate")
    public ApiResponse<Map<String, Object>> simulate(@RequestBody StrategyRequest request) {
        return ApiResponse.success(strategySimulationService.simulatePayload(request));
    }

    @PostMapping("/batch-simulate")
    public ApiResponse<Map<String, Object>> batchSimulate(@RequestBody StrategyBatchRequest request) {
        return ApiResponse.success(strategySimulationService.batchSimulatePayload(request == null ? List.of() : request.strategies()));
    }

    @GetMapping("/revenue/summary")
    public ApiResponse<Map<String, Object>> getRevenueSummary(
        @RequestParam(required = false) String region,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) String resourceUnitId,
        @RequestParam(required = false) String stationId
    ) {
        return ApiResponse.success(strategyRevenueService.getRevenueSummary(region, type, resourceUnitId, stationId));
    }

    @GetMapping("/revenue/detail")
    public ApiResponse<Map<String, Object>> getRevenueDetail(
        @RequestParam(required = false) String region,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) String resourceUnitId,
        @RequestParam(required = false) String stationId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ApiResponse.success(strategyRevenueService.getRevenueDetail(region, type, resourceUnitId, stationId, startDate, endDate));
    }

    @GetMapping("/compare")
    public ApiResponse<Map<String, Object>> compare(@RequestParam(required = false) String ids) {
        List<String> idList = ids == null || ids.isBlank()
            ? List.of()
            : Arrays.stream(ids.split(",")).map(String::trim).filter(value -> !value.isBlank()).toList();
        return ApiResponse.success(strategyRevenueService.compare(idList));
    }
}
