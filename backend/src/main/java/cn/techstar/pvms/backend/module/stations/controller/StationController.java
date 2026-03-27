package cn.techstar.pvms.backend.module.stations.controller;

import cn.techstar.pvms.backend.common.ApiResponse;
import cn.techstar.pvms.backend.module.stations.service.StationMockService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/pvms/resource-units")
public class StationController {

    private final StationMockService stationMockService;

    public StationController(StationMockService stationMockService) {
        this.stationMockService = stationMockService;
    }

    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> getResourceUnitList(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String region,
        @RequestParam(required = false) String capacityRange,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "6") int pageSize
    ) {
        return ApiResponse.success(
            stationMockService.getResourceUnitList(keyword, status, region, capacityRange, page, pageSize)
        );
    }

    @GetMapping("/{resourceUnitId}/overview")
    public ApiResponse<Map<String, Object>> getResourceUnitOverview(@PathVariable String resourceUnitId) {
        return ApiResponse.success(stationMockService.getResourceUnitOverview(resourceUnitId));
    }

    @GetMapping("/{resourceUnitId}/power-curve")
    public ApiResponse<Map<String, Object>> getResourceUnitPowerCurve(
        @PathVariable String resourceUnitId,
        @RequestParam(required = false) String date
    ) {
        return ApiResponse.success(stationMockService.getResourceUnitPowerCurve(resourceUnitId, date));
    }
}
