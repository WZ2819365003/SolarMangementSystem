package cn.techstar.pvms.backend.module.forecast.controller;

import cn.techstar.pvms.backend.common.ApiResponse;
import cn.techstar.pvms.backend.module.forecast.service.ForecastDataService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/pvms/forecast")
public class ForecastController {

    private final ForecastDataService forecastDataService;

    public ForecastController(ForecastDataService forecastDataService) {
        this.forecastDataService = forecastDataService;
    }

    @GetMapping("/meta")
    public ApiResponse<Map<String, Object>> getMeta() {
        return ApiResponse.success(forecastDataService.getMeta());
    }

    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> getOverview(
        @RequestParam(required = false) String region,
        @RequestParam(required = false) String stationId,
        @RequestParam(required = false, defaultValue = "day-ahead") String forecastType
    ) {
        return ApiResponse.success(forecastDataService.getOverview(region, stationId, forecastType));
    }

    @GetMapping("/comparison")
    public ApiResponse<Map<String, Object>> getComparison(
        @RequestParam(required = false) String region,
        @RequestParam(required = false) String stationId
    ) {
        return ApiResponse.success(forecastDataService.getComparison(region, stationId));
    }

    @GetMapping("/deviation-heatmap")
    public ApiResponse<Map<String, Object>> getDeviationHeatmap(
        @RequestParam(required = false) String region,
        @RequestParam(required = false) String stationId,
        @RequestParam(required = false, defaultValue = "day-ahead") String forecastType,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ApiResponse.success(
            forecastDataService.getDeviationHeatmap(region, stationId, forecastType, startDate, endDate)
        );
    }

    @GetMapping("/adjustable")
    public ApiResponse<Map<String, Object>> getAdjustable(
        @RequestParam(required = false) String region,
        @RequestParam(required = false) String stationId,
        @RequestParam(required = false, defaultValue = "day-ahead") String forecastType
    ) {
        return ApiResponse.success(forecastDataService.getAdjustable(region, stationId, forecastType));
    }

    @GetMapping("/accuracy")
    public ApiResponse<Map<String, Object>> getAccuracy(
        @RequestParam(required = false) String region,
        @RequestParam(required = false) String stationId,
        @RequestParam(required = false, defaultValue = "day-ahead") String forecastType,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ApiResponse.success(forecastDataService.getAccuracy(region, stationId, forecastType, startDate, endDate));
    }
}
