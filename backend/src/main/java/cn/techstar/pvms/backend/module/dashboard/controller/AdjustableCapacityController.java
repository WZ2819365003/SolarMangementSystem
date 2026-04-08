package cn.techstar.pvms.backend.module.dashboard.controller;

import cn.techstar.pvms.backend.common.ApiResponse;
import cn.techstar.pvms.backend.module.dashboard.repository.DashboardStationGeoMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/pvms/adjustable-capacity")
public class AdjustableCapacityController {

    private final DashboardStationGeoMapper stationGeoMapper;

    public AdjustableCapacityController(DashboardStationGeoMapper stationGeoMapper) {
        this.stationGeoMapper = stationGeoMapper;
    }

    @GetMapping("/realtime")
    public ApiResponse<Map<String, Object>> getRealtime(
        @RequestParam(required = false) String stationId
    ) {
        List<DashboardStationGeoMapper.StationGeoRow> stations = stationGeoMapper.findAll();
        double totalCap = stations.stream().mapToDouble(DashboardStationGeoMapper.StationGeoRow::capacityKwp).sum();
        // Realtime adjustable capacity: ~15% of total capacity up, ~12% down
        double maxUp   = Math.round(totalCap * 0.15 * 10.0) / 10.0;
        double maxDown = Math.round(totalCap * 0.12 * 10.0) / 10.0;

        // 96-point (15-min) time series for today
        Random rng = new Random(System.currentTimeMillis() / 86400000L);
        List<Map<String, Object>> timeSeries = IntStream.range(0, 96).mapToObj(i -> {
            double upVal   = (i >= 24 && i <= 72) ? Math.round(maxUp   * (0.8 + 0.2 * rng.nextDouble()) * 10) / 10.0 : 0.0;
            double downVal = (i >= 24 && i <= 72) ? Math.round(maxDown * (0.8 + 0.2 * rng.nextDouble()) * 10) / 10.0 : 0.0;
            Map<String, Object> pt = new LinkedHashMap<>();
            pt.put("time", String.format("%02d:%02d", i / 4, (i % 4) * 15));
            pt.put("maxUpCapacity", upVal);
            pt.put("maxDownCapacity", downVal);
            return pt;
        }).toList();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("maxUpCapacity", maxUp);
        data.put("maxDownCapacity", maxDown);
        data.put("timeSeries", timeSeries);
        return ApiResponse.success(data);
    }
}
