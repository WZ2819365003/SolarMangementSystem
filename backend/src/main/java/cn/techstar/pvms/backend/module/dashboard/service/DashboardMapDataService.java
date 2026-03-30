package cn.techstar.pvms.backend.module.dashboard.service;

import cn.techstar.pvms.backend.module.dashboard.repository.DashboardAlarmSnapshotRepository;
import cn.techstar.pvms.backend.module.dashboard.repository.DashboardStationGeoRepository;
import cn.techstar.pvms.backend.module.dashboard.repository.DashboardVppNodeSnapshotRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class DashboardMapDataService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final Map<String, StatusMeta> STATUS_META = buildStatusMeta();

    private final DashboardStationGeoRepository stationGeoRepository;
    private final DashboardAlarmSnapshotRepository alarmSnapshotRepository;
    private final DashboardVppNodeSnapshotRepository vppNodeSnapshotRepository;

    public DashboardMapDataService(
        DashboardStationGeoRepository stationGeoRepository,
        DashboardAlarmSnapshotRepository alarmSnapshotRepository,
        DashboardVppNodeSnapshotRepository vppNodeSnapshotRepository
    ) {
        this.stationGeoRepository = stationGeoRepository;
        this.alarmSnapshotRepository = alarmSnapshotRepository;
        this.vppNodeSnapshotRepository = vppNodeSnapshotRepository;
    }

    public Map<String, Object> getStationsGeo(String status, String region, String capacityRange) {
        List<DashboardStationGeoRepository.StationGeoRow> allStations = stationGeoRepository.findAll();
        CapacityRange range = parseCapacityRange(capacityRange);
        List<DashboardStationGeoRepository.StationGeoRow> filteredStations = allStations.stream()
            .filter(item -> status == null || status.isBlank() || Objects.equals(item.status(), status))
            .filter(item -> region == null || region.isBlank() || Objects.equals(item.region(), region))
            .filter(item -> item.capacityKwp() >= range.min() && item.capacityKwp() <= range.max())
            .toList();

        return orderedMap(
            "filters", orderedMap(
                "statusOptions", buildStatusOptions(),
                "regionOptions", buildRegionOptions(allStations),
                "capacityOptions", buildCapacityOptions()
            ),
            "summary", buildSummary(filteredStations),
            "stations", filteredStations.stream().map(this::mapStation).toList()
        );
    }

    public Map<String, Object> getRecentAlarms(String level, String stationId) {
        DashboardAlarmSnapshotRepository.AlarmSummary summary = alarmSnapshotRepository.summary();
        List<DashboardAlarmSnapshotRepository.AlarmSnapshotRow> items = alarmSnapshotRepository.findRecent(level, stationId);

        return orderedMap(
            "summary", orderedMap(
                "critical", summary.critical(),
                "major", summary.major(),
                "minor", summary.minor(),
                "hint", summary.hint()
            ),
            "items", items.stream().map(this::mapAlarm).toList()
        );
    }

    public Map<String, Object> getVppNodeStatus() {
        DashboardVppNodeSnapshotRepository.VppNodeSnapshotRow node = vppNodeSnapshotRepository.findDefault();
        List<DashboardStationGeoRepository.StationGeoRow> stations = stationGeoRepository.findAll();
        double availableCapacityMw = stations.stream()
            .filter(item -> !"offline".equals(item.status()) && !"fault".equals(item.status()))
            .mapToDouble(DashboardStationGeoRepository.StationGeoRow::capacityKwp)
            .sum() / 1000.0;
        long onlineStations = stations.stream()
            .filter(item -> !"offline".equals(item.status()))
            .count();

        return orderedMap(
            "nodeId", node.nodeId(),
            "totalCapacityMw", round(node.totalCapacityMw(), 1),
            "availableCapacityMw", round(availableCapacityMw, 1),
            "onlineStations", onlineStations,
            "totalStations", stations.size(),
            "adjustableRangeMw", orderedMap(
                "min", round(node.adjustableMinMw(), 1),
                "max", round(node.adjustableMaxMw(), 1)
            ),
            "lastHeartbeat", formatDateTime(node.lastHeartbeat())
        );
    }

    private List<Map<String, Object>> buildStatusOptions() {
        List<Map<String, Object>> options = new ArrayList<>();
        options.add(option("全部状态", ""));
        STATUS_META.forEach((key, meta) -> options.add(option(meta.label(), key)));
        return options;
    }

    private List<Map<String, Object>> buildRegionOptions(List<DashboardStationGeoRepository.StationGeoRow> stations) {
        LinkedHashSet<String> regions = new LinkedHashSet<>();
        stations.stream().map(DashboardStationGeoRepository.StationGeoRow::region).forEach(regions::add);

        List<Map<String, Object>> options = new ArrayList<>();
        options.add(option("全部区域", ""));
        regions.forEach(region -> options.add(option(region, region)));
        return options;
    }

    private List<Map<String, Object>> buildCapacityOptions() {
        return List.of(
            option("全部容量", ""),
            option("3MW 以下", "lt3000"),
            option("3MW - 5MW", "3000to5000"),
            option("5MW 以上", "gte5000")
        );
    }

    private List<Map<String, Object>> buildSummary(List<DashboardStationGeoRepository.StationGeoRow> stations) {
        List<Map<String, Object>> items = new ArrayList<>();
        STATUS_META.forEach((status, meta) -> items.add(orderedMap(
            "key", status,
            "label", meta.label(),
            "color", meta.color(),
            "count", (int) stations.stream().filter(item -> Objects.equals(item.status(), status)).count()
        )));
        return items;
    }

    private Map<String, Object> mapStation(DashboardStationGeoRepository.StationGeoRow station) {
        StatusMeta meta = STATUS_META.getOrDefault(station.status(), new StatusMeta(station.status(), "#909399"));
        return orderedMap(
            "id", station.id(),
            "name", station.name(),
            "resourceUnitId", station.resourceUnitId(),
            "resourceUnitName", station.resourceUnitName(),
            "region", station.region(),
            "longitude", station.longitude(),
            "latitude", station.latitude(),
            "status", station.status(),
            "statusLabel", meta.label(),
            "statusColor", meta.color(),
            "capacityKwp", station.capacityKwp(),
            "realtimePowerKw", station.realtimePowerKw(),
            "todayEnergyKwh", station.todayEnergyKwh(),
            "todayRevenueCny", station.todayRevenueCny(),
            "healthGrade", station.healthGrade(),
            "availability", station.availability(),
            "address", station.address()
        );
    }

    private Map<String, Object> mapAlarm(DashboardAlarmSnapshotRepository.AlarmSnapshotRow alarm) {
        return orderedMap(
            "id", alarm.id(),
            "time", formatTime(alarm.eventTime()),
            "level", alarm.level(),
            "levelLabel", alarm.levelLabel(),
            "deviceName", alarm.deviceName(),
            "stationId", alarm.stationId(),
            "stationName", alarm.stationName(),
            "alarmType", alarm.alarmType(),
            "description", alarm.description(),
            "status", alarm.status(),
            "owner", alarm.owner(),
            "suggestion", alarm.suggestion()
        );
    }

    private CapacityRange parseCapacityRange(String capacityRange) {
        if (capacityRange == null || capacityRange.isBlank()) {
            return new CapacityRange(0, Double.MAX_VALUE);
        }
        return switch (capacityRange) {
            case "lt3000" -> new CapacityRange(0, 3000);
            case "3000to5000" -> new CapacityRange(3000, 5000);
            case "gte5000" -> new CapacityRange(5000, Double.MAX_VALUE);
            default -> new CapacityRange(0, Double.MAX_VALUE);
        };
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "" : DATE_TIME_FORMATTER.format(value);
    }

    private String formatTime(LocalDateTime value) {
        return value == null ? "" : TIME_FORMATTER.format(value);
    }

    private double round(double value, int scale) {
        double factor = Math.pow(10, scale);
        return Math.round(value * factor) / factor;
    }

    private Map<String, Object> option(String label, String value) {
        return orderedMap("label", label, "value", value);
    }

    private Map<String, Object> orderedMap(Object... entries) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            result.put((String) entries[index], entries[index + 1]);
        }
        return result;
    }

    private static Map<String, StatusMeta> buildStatusMeta() {
        LinkedHashMap<String, StatusMeta> meta = new LinkedHashMap<>();
        meta.put("normal", new StatusMeta("正常", "#67C23A"));
        meta.put("warning", new StatusMeta("告警", "#E6A23C"));
        meta.put("fault", new StatusMeta("故障", "#F56C6C"));
        meta.put("maintenance", new StatusMeta("检修", "#409EFF"));
        meta.put("offline", new StatusMeta("离线", "#909399"));
        return meta;
    }

    private record StatusMeta(String label, String color) {
    }

    private record CapacityRange(double min, double max) {
    }
}
