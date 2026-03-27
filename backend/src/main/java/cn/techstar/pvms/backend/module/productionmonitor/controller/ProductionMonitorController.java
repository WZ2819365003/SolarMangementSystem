package cn.techstar.pvms.backend.module.productionmonitor.controller;

import cn.techstar.pvms.backend.common.ApiResponse;
import cn.techstar.pvms.backend.module.productionmonitor.service.ProductionMonitorMockService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/pvms/production-monitor")
public class ProductionMonitorController {

    private final ProductionMonitorMockService productionMonitorMockService;

    public ProductionMonitorController(ProductionMonitorMockService productionMonitorMockService) {
        this.productionMonitorMockService = productionMonitorMockService;
    }

    @GetMapping("/meta")
    public ApiResponse<Map<String, Object>> getMeta() {
        return ApiResponse.success(productionMonitorMockService.getMeta());
    }

    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> getOverview(@RequestParam String resourceUnitId) {
        return ApiResponse.success(productionMonitorMockService.getOverview(resourceUnitId));
    }

    @GetMapping("/output")
    public ApiResponse<Map<String, Object>> getOutput(
        @RequestParam String resourceUnitId,
        @RequestParam(required = false, defaultValue = "15m") String granularity
    ) {
        return ApiResponse.success(productionMonitorMockService.getOutput(resourceUnitId, granularity));
    }

    @GetMapping("/dispatch")
    public ApiResponse<Map<String, Object>> getDispatch(@RequestParam String resourceUnitId) {
        return ApiResponse.success(productionMonitorMockService.getDispatch(resourceUnitId));
    }

    @GetMapping("/weather")
    public ApiResponse<Map<String, Object>> getWeather(@RequestParam String resourceUnitId) {
        return ApiResponse.success(productionMonitorMockService.getWeather(resourceUnitId));
    }
}
