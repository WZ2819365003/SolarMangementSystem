package cn.techstar.pvms.backend.module.devicealarm.controller;

import cn.techstar.pvms.backend.common.ApiResponse;
import cn.techstar.pvms.backend.module.devicealarm.service.DeviceAlarmDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DeviceAlarmController {

    private final DeviceAlarmDataService dataService;

    public DeviceAlarmController(DeviceAlarmDataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/api/pvms/devices/monitor")
    public ApiResponse<Map<String, Object>> getDeviceMonitor(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String status
    ) {
        return ApiResponse.success(dataService.getDeviceMonitor(keyword, status));
    }

    @GetMapping("/api/pvms/alarms/center")
    public ApiResponse<Map<String, Object>> getAlarmCenter(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String level,
        @RequestParam(required = false) String status
    ) {
        return ApiResponse.success(dataService.getAlarmCenter(keyword, level, status));
    }
}
