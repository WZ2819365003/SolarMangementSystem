package cn.techstar.pvms.backend.module.productionmonitor.controller;

import cn.techstar.pvms.backend.common.ApiResponse;
import cn.techstar.pvms.backend.module.productionmonitor.service.ProductionMonitorDataService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/pvms/production-monitor")
public class ProductionMonitorController {

    private final ProductionMonitorDataService productionMonitorDataService;

    public ProductionMonitorController(ProductionMonitorDataService productionMonitorDataService) {
        this.productionMonitorDataService = productionMonitorDataService;
    }

    @GetMapping("/meta")
    public ApiResponse<Map<String, Object>> getMeta() {
        return ApiResponse.success(productionMonitorDataService.getMeta());
    }

    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> getOverview(@RequestParam String resourceUnitId) {
        return ApiResponse.success(productionMonitorDataService.getOverview(resourceUnitId));
    }

    @GetMapping("/output")
    public ApiResponse<Map<String, Object>> getOutput(
        @RequestParam String resourceUnitId,
        @RequestParam(required = false, defaultValue = "15m") String granularity
    ) {
        return ApiResponse.success(productionMonitorDataService.getOutput(resourceUnitId, granularity));
    }

    @GetMapping("/load")
    public ApiResponse<Map<String, Object>> getLoad(
        @RequestParam(required = false) String resourceUnitId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam(required = false, defaultValue = "15m") String granularity
    ) {
        return ApiResponse.success(productionMonitorDataService.getLoad(resourceUnitId, date, granularity));
    }

    @GetMapping("/dispatch")
    public ApiResponse<Map<String, Object>> getDispatch(@RequestParam String resourceUnitId) {
        return ApiResponse.success(productionMonitorDataService.getDispatch(resourceUnitId));
    }

    @GetMapping("/grid-interaction")
    public ApiResponse<Map<String, Object>> getGridInteraction(@RequestParam String stationId) {
        return ApiResponse.success(productionMonitorDataService.getGridInteraction(stationId));
    }

    @GetMapping("/weather")
    public ApiResponse<Map<String, Object>> getWeather(@RequestParam String resourceUnitId) {
        return ApiResponse.success(productionMonitorDataService.getWeather(resourceUnitId));
    }
}
