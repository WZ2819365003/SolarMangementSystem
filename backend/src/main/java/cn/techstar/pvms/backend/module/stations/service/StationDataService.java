package cn.techstar.pvms.backend.module.stations.service;

import cn.techstar.pvms.backend.module.productionmonitor.repository.ProductionMonitorCurveMapper;
import cn.techstar.pvms.backend.module.productionmonitor.repository.ProductionMonitorResourceUnitMapper;
import cn.techstar.pvms.backend.module.productionmonitor.repository.ProductionMonitorStationSnapshotMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class StationDataService {

    private final ProductionMonitorResourceUnitMapper resourceUnitMapper;
    private final ProductionMonitorStationSnapshotMapper stationSnapshotMapper;
    private final ProductionMonitorCurveMapper curveMapper;

    public StationDataService(
        ProductionMonitorResourceUnitMapper resourceUnitMapper,
        ProductionMonitorStationSnapshotMapper stationSnapshotMapper,
        ProductionMonitorCurveMapper curveMapper
    ) {
        this.resourceUnitMapper = resourceUnitMapper;
        this.stationSnapshotMapper = stationSnapshotMapper;
        this.curveMapper = curveMapper;
    }

    public Map<String, Object> getResourceUnitList(
        String keyword, String status, String region, String capacityRange,
        int page, int pageSize
    ) {
        List<ProductionMonitorResourceUnitMapper.ResourceUnitRow> allUnits = resourceUnitMapper.findAll();
        List<ProductionMonitorStationSnapshotMapper.StationSnapshotRow> allSnapshots = stationSnapshotMapper.findAll();

        // group stations by resource unit
        Map<String, List<ProductionMonitorStationSnapshotMapper.StationSnapshotRow>> stationsByUnit =
            allSnapshots.stream().collect(Collectors.groupingBy(
                ProductionMonitorStationSnapshotMapper.StationSnapshotRow::resourceUnitId));

        // filter
        List<ProductionMonitorResourceUnitMapper.ResourceUnitRow> filtered = allUnits.stream()
            .filter(u -> keyword == null || keyword.isBlank()
                || u.name().contains(keyword) || u.id().contains(keyword))
            .filter(u -> status == null || status.isBlank() || u.status().equals(status))
            .filter(u -> region == null || region.isBlank() || u.region().equals(region))
            .filter(u -> {
                if (capacityRange == null || capacityRange.isBlank()) return true;
                double totalMw = stationsByUnit.getOrDefault(u.id(), List.of()).stream()
                    .mapToDouble(ProductionMonitorStationSnapshotMapper.StationSnapshotRow::capacityMw)
                    .sum();
                return matchCapacityRange(totalMw, capacityRange);
            })
            .toList();

        int total = filtered.size();
        int fromIndex = Math.min((page - 1) * pageSize, total);
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<ProductionMonitorResourceUnitMapper.ResourceUnitRow> pageItems = filtered.subList(fromIndex, toIndex);

        List<Map<String, Object>> items = pageItems.stream().map(u -> {
            List<ProductionMonitorStationSnapshotMapper.StationSnapshotRow> stations =
                stationsByUnit.getOrDefault(u.id(), List.of());
            double totalCapacity = stations.stream()
                .mapToDouble(ProductionMonitorStationSnapshotMapper.StationSnapshotRow::capacityMw).sum();
            double totalPower = stations.stream()
                .mapToDouble(ProductionMonitorStationSnapshotMapper.StationSnapshotRow::realtimePowerKw).sum();

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", u.id());
            item.put("name", u.name());
            item.put("region", u.region());
            item.put("city", u.city());
            item.put("status", u.status());
            item.put("capacityMw", Math.round(totalCapacity * 100.0) / 100.0);
            item.put("realtimePowerKw", Math.round(totalPower * 100.0) / 100.0);
            item.put("stationCount", stations.size());
            item.put("dispatchMode", u.dispatchMode());
            item.put("strategyLabel", u.strategyLabel());
            item.put("alarmTotal", u.alarmTotal());
            item.put("alarmCritical", u.alarmCritical());
            item.put("alarmMajor", u.alarmMajor());
            item.put("alarmMinor", u.alarmMinor());
            return item;
        }).toList();

        return Map.of(
            "items", items,
            "total", total,
            "page", page,
            "pageSize", pageSize
        );
    }

    public Map<String, Object> getResourceUnitOverview(String resourceUnitId) {
        List<ProductionMonitorStationSnapshotMapper.StationSnapshotRow> stations =
            stationSnapshotMapper.findAll().stream()
                .filter(s -> s.resourceUnitId().equals(resourceUnitId))
                .toList();

        double totalCapacity = stations.stream()
            .mapToDouble(ProductionMonitorStationSnapshotMapper.StationSnapshotRow::capacityMw).sum();
        double totalPower = stations.stream()
            .mapToDouble(ProductionMonitorStationSnapshotMapper.StationSnapshotRow::realtimePowerKw).sum();
        long onlineCount = stations.stream()
            .filter(s -> "online".equals(s.status())).count();

        ProductionMonitorResourceUnitMapper.ResourceUnitRow unit = resourceUnitMapper.findAll().stream()
            .filter(u -> u.id().equals(resourceUnitId)).findFirst().orElse(null);

        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("resourceUnitId", resourceUnitId);
        overview.put("name", unit != null ? unit.name() : resourceUnitId);
        overview.put("region", unit != null ? unit.region() : "");
        overview.put("capacityMw", Math.round(totalCapacity * 100.0) / 100.0);
        overview.put("realtimePowerKw", Math.round(totalPower * 100.0) / 100.0);
        overview.put("stationCount", stations.size());
        overview.put("onlineCount", onlineCount);
        overview.put("stations", stations.stream().map(s -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.id());
            m.put("name", s.name());
            m.put("capacityMw", s.capacityMw());
            m.put("status", s.status());
            m.put("realtimePowerKw", s.realtimePowerKw());
            m.put("availability", s.availability());
            m.put("healthGrade", s.healthGrade());
            return m;
        }).toList());
        return overview;
    }

    public Map<String, Object> getResourceUnitPowerCurve(String resourceUnitId, String date) {
        LocalDate bizDate = date != null && !date.isBlank()
            ? LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
            : LocalDate.now();

        List<ProductionMonitorCurveMapper.CurveRow> curves =
            curveMapper.findByResourceUnitIdAndDate(resourceUnitId, bizDate);

        // aggregate by time slot
        Map<Integer, double[]> slotAgg = new TreeMap<>();
        for (var c : curves) {
            slotAgg.computeIfAbsent(c.timeSlot(), k -> new double[3]);
            double[] arr = slotAgg.get(c.timeSlot());
            arr[0] += c.pvPowerKw();
            arr[1] += c.forecastPowerKw();
            arr[2] += c.loadKw();
        }

        List<String> timeLabels = new ArrayList<>();
        List<Double> pvPower = new ArrayList<>();
        List<Double> forecastPower = new ArrayList<>();
        List<Double> load = new ArrayList<>();

        for (var entry : slotAgg.entrySet()) {
            int slot = entry.getKey();
            int hour = slot / 4;
            int minute = (slot % 4) * 15;
            timeLabels.add(String.format("%02d:%02d", hour, minute));
            pvPower.add(Math.round(entry.getValue()[0] * 100.0) / 100.0);
            forecastPower.add(Math.round(entry.getValue()[1] * 100.0) / 100.0);
            load.add(Math.round(entry.getValue()[2] * 100.0) / 100.0);
        }

        // fill empty with simulated solar curve if no data
        if (slotAgg.isEmpty()) {
            IntStream.range(0, 96).forEach(slot -> {
                int hour = slot / 4;
                int minute = (slot % 4) * 15;
                timeLabels.add(String.format("%02d:%02d", hour, minute));
                double h = slot / 4.0;
                double pv = h >= 6 && h <= 18 ? 80.0 * Math.sin(Math.PI * (h - 6) / 12) : 0;
                pvPower.add(Math.round(pv * 100.0) / 100.0);
                forecastPower.add(Math.round(pv * 0.95 * 100.0) / 100.0);
                load.add(Math.round((30 + 20 * Math.sin(Math.PI * h / 12)) * 100.0) / 100.0);
            });
        }

        return Map.of(
            "resourceUnitId", resourceUnitId,
            "date", bizDate.toString(),
            "timeLabels", timeLabels,
            "pvPower", pvPower,
            "forecastPower", forecastPower,
            "load", load
        );
    }

    private boolean matchCapacityRange(double capacityMw, String range) {
        return switch (range) {
            case "small" -> capacityMw < 1;
            case "medium" -> capacityMw >= 1 && capacityMw < 10;
            case "large" -> capacityMw >= 10;
            default -> true;
        };
    }
}
