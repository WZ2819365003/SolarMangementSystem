package cn.techstar.pvms.backend.module.stationarchive.controller;

import cn.techstar.pvms.backend.common.ApiResponse;
import cn.techstar.pvms.backend.module.stationarchive.service.StationArchiveDataService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
public class StationArchiveController {

    private final StationArchiveDataService service;

    public StationArchiveController(StationArchiveDataService service) {
        this.service = service;
    }

    @GetMapping("/api/pvms/stations/archive")
    public ApiResponse<Map<String, Object>> getArchiveList(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String gridStatus
    ) {
        return ApiResponse.success(service.getArchiveList(keyword, gridStatus));
    }

    @GetMapping("/api/pvms/station-tree")
    public ApiResponse<Map<String, Object>> getStationTree(
        @RequestParam(required = false) String status
    ) {
        return ApiResponse.success(service.getStationTree(status));
    }

    @GetMapping("/api/pvms/station-archive/company-overview")
    public ApiResponse<Map<String, Object>> getCompanyOverview(
        @RequestParam String companyId
    ) {
        return ApiResponse.success(service.getCompanyOverview(companyId));
    }

    @GetMapping("/api/pvms/station-archive/resource-overview")
    public ApiResponse<Map<String, Object>> getResourceOverview(
        @RequestParam String stationId
    ) {
        return ApiResponse.success(service.getResourceOverview(stationId));
    }

    @GetMapping("/api/pvms/station-archive/station-realtime")
    public ApiResponse<Map<String, Object>> getStationRealtime(
        @RequestParam String stationId,
        @RequestParam(required = false) String metric,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam(required = false) String granularity
    ) {
        return ApiResponse.success(service.getStationRealtime(stationId, metric, date, granularity));
    }

    @GetMapping("/api/pvms/station-archive/station-adjustable")
    public ApiResponse<Map<String, Object>> getStationAdjustable(
        @RequestParam String stationId
    ) {
        return ApiResponse.success(service.getStationAdjustable(stationId));
    }

    @GetMapping("/api/pvms/station-archive/station-strategy")
    public ApiResponse<Map<String, Object>> getStationStrategy(
        @RequestParam String stationId
    ) {
        return ApiResponse.success(service.getStationStrategy(stationId));
    }

    @GetMapping("/api/pvms/station-archive/inverter-realtime")
    public ApiResponse<Map<String, Object>> getInverterRealtime(
        @RequestParam(required = false) String stationId,
        @RequestParam(required = false) String inverterId
    ) {
        String resolvedInverterId = (inverterId == null || inverterId.isBlank()) ? stationId : inverterId;
        return ApiResponse.success(service.getInverterRealtime(resolvedInverterId));
    }
}
