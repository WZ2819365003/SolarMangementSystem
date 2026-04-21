package cn.techstar.pvms.backend.module.dashboard.controller;

import cn.techstar.pvms.backend.common.ApiResponse;
import cn.techstar.pvms.backend.module.dashboard.service.DashboardMapDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/pvms/alarms")
public class AlarmController {

    private final DashboardMapDataService dashboardMapDataService;

    public AlarmController(DashboardMapDataService dashboardMapDataService) {
        this.dashboardMapDataService = dashboardMapDataService;
    }

    @GetMapping("/recent")
    public ApiResponse<Map<String, Object>> getRecentAlarms(
        @RequestParam(required = false) String level,
        @RequestParam(required = false) String stationId
    ) {
        return ApiResponse.success(dashboardMapDataService.getRecentAlarms(level, stationId));
    }
}
