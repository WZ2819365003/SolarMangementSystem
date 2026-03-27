package cn.techstar.pvms.backend.module.dashboard.controller;

import cn.techstar.pvms.backend.common.ApiResponse;
import cn.techstar.pvms.backend.module.dashboard.service.DashboardMockService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/pvms/dashboard")
public class DashboardController {

    private final DashboardMockService dashboardMockService;

    public DashboardController(DashboardMockService dashboardMockService) {
        this.dashboardMockService = dashboardMockService;
    }

    @GetMapping("/stations-geo")
    public ApiResponse<Map<String, Object>> getStationsGeo(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String region,
        @RequestParam(required = false) String capacityRange
    ) {
        return ApiResponse.success(dashboardMockService.getStationsGeo(status, region, capacityRange));
    }

    @GetMapping("/kpi-summary")
    public ApiResponse<Map<String, Object>> getKpiSummary(
        @RequestParam(required = false) String stationId
    ) {
        return ApiResponse.success(dashboardMockService.getKpiSummary(stationId));
    }

    @GetMapping("/power-curve")
    public ApiResponse<Map<String, Object>> getPowerCurve(
        @RequestParam(required = false) String stationId,
        @RequestParam(required = false) String date
    ) {
        return ApiResponse.success(dashboardMockService.getPowerCurve(stationId, date));
    }

    @GetMapping("/station-ranking")
    public ApiResponse<Map<String, Object>> getStationRanking(
        @RequestParam(required = false) String metric
    ) {
        return ApiResponse.success(dashboardMockService.getStationRanking(metric));
    }
}
