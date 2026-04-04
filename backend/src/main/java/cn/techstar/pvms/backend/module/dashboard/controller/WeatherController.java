package cn.techstar.pvms.backend.module.dashboard.controller;

import cn.techstar.pvms.backend.common.ApiResponse;
import cn.techstar.pvms.backend.module.dashboard.service.DashboardMapDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/pvms/weather")
public class WeatherController {

    private final DashboardMapDataService dashboardMapDataService;

    public WeatherController(DashboardMapDataService dashboardMapDataService) {
        this.dashboardMapDataService = dashboardMapDataService;
    }

    @GetMapping("/current")
    public ApiResponse<Map<String, Object>> getCurrentWeather(
        @RequestParam(required = false) String stationId
    ) {
        return ApiResponse.success(dashboardMapDataService.getWeather(stationId));
    }
}
