package cn.techstar.pvms.backend.module.dashboard.controller;

import cn.techstar.pvms.backend.common.ApiResponse;
import cn.techstar.pvms.backend.module.dashboard.service.DashboardMapDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/pvms/dashboard")
public class DashboardController {

    private final DashboardMapDataService dashboardMapDataService;

    public DashboardController(DashboardMapDataService dashboardMapDataService) {
        this.dashboardMapDataService = dashboardMapDataService;
    }

    @GetMapping("/stations-geo")
    public ApiResponse<Map<String, Object>> getStationsGeo(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String region,
        @RequestParam(required = false) String capacityRange
    ) {
        return ApiResponse.success(dashboardMapDataService.getStationsGeo(status, region, capacityRange));
    }

    @GetMapping("/kpi-summary")
    public ApiResponse<Map<String, Object>> getKpiSummary(
        @RequestParam(required = false) String stationId
    ) {
        return ApiResponse.success(dashboardMapDataService.getKpiSummary(stationId));
    }

    @GetMapping("/power-curve")
    public ApiResponse<Map<String, Object>> getPowerCurve(
        @RequestParam(required = false) String stationId,
        @RequestParam(required = false) String date
    ) {
        return ApiResponse.success(dashboardMapDataService.getPowerCurve(stationId, date));
    }

    @GetMapping("/station-ranking")
    public ApiResponse<Map<String, Object>> getStationRanking(
        @RequestParam(required = false) String metric
    ) {
        return ApiResponse.success(dashboardMapDataService.getStationRanking(metric));
    }

    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> getOverview() {
        return ApiResponse.success(dashboardMapDataService.getOverview());
    }

    @GetMapping("/vpp-node-status")
    public ApiResponse<Map<String, Object>> getVppNodeStatus() {
        return ApiResponse.success(dashboardMapDataService.getVppNodeStatus());
    }
}
