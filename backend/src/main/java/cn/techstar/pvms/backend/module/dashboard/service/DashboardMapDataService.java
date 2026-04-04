package cn.techstar.pvms.backend.module.dashboard.service;

import cn.techstar.pvms.backend.module.dashboard.repository.DashboardAlarmSnapshotMapper;
import cn.techstar.pvms.backend.module.dashboard.repository.DashboardStationGeoMapper;
import cn.techstar.pvms.backend.module.dashboard.repository.DashboardVppNodeSnapshotMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.IntStream;

@Service
public class DashboardMapDataService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final Map<String, StatusMeta> STATUS_META = buildStatusMeta();

    private final DashboardStationGeoMapper stationGeoMapper;
    private final DashboardAlarmSnapshotMapper alarmSnapshotMapper;
    private final DashboardVppNodeSnapshotMapper vppNodeSnapshotMapper;

    public DashboardMapDataService(
        DashboardStationGeoMapper stationGeoMapper,
        DashboardAlarmSnapshotMapper alarmSnapshotMapper,
        DashboardVppNodeSnapshotMapper vppNodeSnapshotMapper
    ) {
        this.stationGeoMapper = stationGeoMapper;
        this.alarmSnapshotMapper = alarmSnapshotMapper;
        this.vppNodeSnapshotMapper = vppNodeSnapshotMapper;
    }

    public Map<String, Object> getStationsGeo(String status, String region, String capacityRange) {
        List<DashboardStationGeoMapper.StationGeoRow> allStations = stationGeoMapper.findAll();
        CapacityRange range = parseCapacityRange(capacityRange);
        List<DashboardStationGeoMapper.StationGeoRow> filteredStations = allStations.stream()
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
        DashboardAlarmSnapshotMapper.AlarmSummary summary = alarmSnapshotMapper.summary();
        List<DashboardAlarmSnapshotMapper.AlarmSnapshotRow> items = alarmSnapshotMapper.findRecent(level, stationId);

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
        DashboardVppNodeSnapshotMapper.VppNodeSnapshotRow node = vppNodeSnapshotMapper.findDefault();
        List<DashboardStationGeoMapper.StationGeoRow> stations = stationGeoMapper.findAll();
        double availableCapacityMw = stations.stream()
            .filter(item -> !"offline".equals(item.status()) && !"fault".equals(item.status()))
            .mapToDouble(DashboardStationGeoMapper.StationGeoRow::capacityKwp)
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

    private List<Map<String, Object>> buildRegionOptions(List<DashboardStationGeoMapper.StationGeoRow> stations) {
        LinkedHashSet<String> regions = new LinkedHashSet<>();
        stations.stream().map(DashboardStationGeoMapper.StationGeoRow::region).forEach(regions::add);

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

    private List<Map<String, Object>> buildSummary(List<DashboardStationGeoMapper.StationGeoRow> stations) {
        List<Map<String, Object>> items = new ArrayList<>();
        STATUS_META.forEach((status, meta) -> items.add(orderedMap(
            "key", status,
            "label", meta.label(),
            "color", meta.color(),
            "count", (int) stations.stream().filter(item -> Objects.equals(item.status(), status)).count()
        )));
        return items;
    }

    private Map<String, Object> mapStation(DashboardStationGeoMapper.StationGeoRow station) {
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

    private Map<String, Object> mapAlarm(DashboardAlarmSnapshotMapper.AlarmSnapshotRow alarm) {
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

    // ============= KPI Summary =============
    public Map<String, Object> getKpiSummary(String stationId) {
        List<DashboardStationGeoMapper.StationGeoRow> stations = stationGeoMapper.findAll();
        if (stationId != null && !stationId.isBlank()) {
            stations = stations.stream().filter(s -> s.id().equals(stationId)).toList();
        }
        String focusLabel = (stationId == null || stationId.isBlank()) ? "全系统"
            : stations.stream().findFirst().map(DashboardStationGeoMapper.StationGeoRow::name).orElse("未知");
        double totalCapacity = stations.stream().mapToDouble(DashboardStationGeoMapper.StationGeoRow::capacityKwp).sum();
        double totalPower = stations.stream().mapToDouble(DashboardStationGeoMapper.StationGeoRow::realtimePowerKw).sum();
        double totalEnergy = stations.stream().mapToDouble(DashboardStationGeoMapper.StationGeoRow::todayEnergyKwh).sum();
        double totalRevenue = stations.stream().mapToDouble(DashboardStationGeoMapper.StationGeoRow::todayRevenueCny).sum();
        double avgAvailability = stations.stream().mapToDouble(DashboardStationGeoMapper.StationGeoRow::availability).average().orElse(0);
        return orderedMap(
            "focusLabel", focusLabel,
            "items", List.of(
                kpiItem("capacity", "装机容量", round(totalCapacity / 1000, 2), "MW"),
                kpiItem("power", "实时功率", round(totalPower / 1000, 2), "MW"),
                kpiItem("energy", "今日发电量", round(totalEnergy / 1000, 1), "MWh"),
                kpiItem("revenue", "今日收益", round(totalRevenue, 0), "元"),
                kpiItem("hours", "等效利用小时", round(totalPower > 0 ? totalEnergy / totalPower : 0, 1), "h"),
                kpiItem("availability", "平均可用率", round(avgAvailability, 1), "%"),
                kpiItem("co2", "CO₂减排", round(totalEnergy * 0.785 / 1000, 1), "t"),
                kpiItem("deviation", "功率偏差", round(3.2, 1), "%")
            )
        );
    }

    // ============= Power Curve =============
    public Map<String, Object> getPowerCurve(String stationId, String date) {
        String currentDate = (date != null && !date.isBlank()) ? date : LocalDate.now().toString();
        List<DashboardStationGeoMapper.StationGeoRow> all = stationGeoMapper.findAll();
        DashboardStationGeoMapper.StationGeoRow station = (stationId != null && !stationId.isBlank())
            ? all.stream().filter(s -> s.id().equals(stationId)).findFirst().orElse(all.isEmpty() ? null : all.get(0))
            : (all.isEmpty() ? null : all.get(0));
        String name = station != null ? station.name() : "系统";
        double cap = station != null ? station.capacityKwp() : all.stream().mapToDouble(DashboardStationGeoMapper.StationGeoRow::capacityKwp).sum();
        long seed = currentDate.hashCode();
        Random rng = new Random(seed);
        List<Double> actual = IntStream.range(0, 96).mapToObj(i -> round(solarCurve(i, cap, rng, 0.9), 1)).toList();
        List<Double> plan = IntStream.range(0, 96).mapToObj(i -> round(solarCurve(i, cap, rng, 1.0), 1)).toList();
        return orderedMap("currentDate", currentDate, "stationName", name, "actual", actual, "plan", plan);
    }

    // ============= Station Ranking =============
    public Map<String, Object> getStationRanking(String metric) {
        String m = (metric != null && !metric.isBlank()) ? metric : "energy";
        List<DashboardStationGeoMapper.StationGeoRow> stations = stationGeoMapper.findAll();
        List<Map<String, Object>> rankings = stations.stream().map(s -> {
            double value = switch (m) {
                case "hours" -> s.todayEnergyKwh() / Math.max(s.capacityKwp(), 1) * 24;
                case "pr" -> s.availability();
                default -> s.todayEnergyKwh();
            };
            return orderedMap("id", (Object) s.id(), "name", s.name(), "value", round(value, 1));
        }).sorted((a, b) -> Double.compare((double) b.get("value"), (double) a.get("value"))).toList();
        return orderedMap(
            "currentMetric", m,
            "rankings", rankings,
            "metricOptions", List.of(
                orderedMap("key", "energy", "label", "发电量"),
                orderedMap("key", "hours", "label", "利用小时"),
                orderedMap("key", "pr", "label", "PR值")
            )
        );
    }

    // ============= Overview =============
    public Map<String, Object> getOverview() {
        List<DashboardStationGeoMapper.StationGeoRow> stations = stationGeoMapper.findAll();
        DashboardAlarmSnapshotMapper.AlarmSummary alarmSummary = alarmSnapshotMapper.summary();
        List<DashboardAlarmSnapshotMapper.AlarmSnapshotRow> alarms = alarmSnapshotMapper.findRecent(null, null);
        List<Map<String, Object>> summaryCards = List.of(
            orderedMap("key", "totalCapacity", "label", "总装机", "value", round(stations.stream().mapToDouble(DashboardStationGeoMapper.StationGeoRow::capacityKwp).sum() / 1000, 1), "unit", "MW"),
            orderedMap("key", "totalPower", "label", "实时功率", "value", round(stations.stream().mapToDouble(DashboardStationGeoMapper.StationGeoRow::realtimePowerKw).sum() / 1000, 1), "unit", "MW"),
            orderedMap("key", "todayEnergy", "label", "今日发电", "value", round(stations.stream().mapToDouble(DashboardStationGeoMapper.StationGeoRow::todayEnergyKwh).sum() / 1000, 1), "unit", "MWh"),
            orderedMap("key", "todayRevenue", "label", "今日收益", "value", round(stations.stream().mapToDouble(DashboardStationGeoMapper.StationGeoRow::todayRevenueCny).sum(), 0), "unit", "元"),
            orderedMap("key", "alarmCount", "label", "告警数", "value", alarmSummary.critical() + alarmSummary.major() + alarmSummary.minor() + alarmSummary.hint(), "unit", "条")
        );
        List<String> dates = IntStream.rangeClosed(1, 7).mapToObj(i -> LocalDate.now().minusDays(7 - i).toString()).toList();
        Random rng = new Random(42);
        List<Double> energy = dates.stream().map(d -> round(800 + rng.nextDouble() * 400, 0)).toList();
        return orderedMap(
            "summaryCards", summaryCards,
            "trends", orderedMap("dates", dates, "energy", energy),
            "focusAlarms", alarms.stream().limit(5).map(this::mapAlarm).toList(),
            "stationRows", stations.stream().map(s -> orderedMap(
                "id", s.id(), "name", s.name(), "status", s.status(),
                "power", round(s.realtimePowerKw(), 1), "energy", round(s.todayEnergyKwh(), 1)
            )).toList()
        );
    }

    // ============= Weather =============
    public Map<String, Object> getWeather(String stationId) {
        List<DashboardStationGeoMapper.StationGeoRow> all = stationGeoMapper.findAll();
        DashboardStationGeoMapper.StationGeoRow station = (stationId != null && !stationId.isBlank())
            ? all.stream().filter(s -> s.id().equals(stationId)).findFirst().orElse(all.isEmpty() ? null : all.get(0))
            : (all.isEmpty() ? null : all.get(0));
        String name = station != null ? station.name() : "未知";
        return orderedMap(
            "stationName", name,
            "current", orderedMap("weather", "晴", "temperature", 28, "humidity", 55, "irradiance", 850, "windSpeed", 2.3),
            "forecast", List.of(
                orderedMap("date", LocalDate.now().plusDays(1).toString(), "weather", "多云", "high", 30, "low", 22),
                orderedMap("date", LocalDate.now().plusDays(2).toString(), "weather", "晴", "high", 32, "low", 23),
                orderedMap("date", LocalDate.now().plusDays(3).toString(), "weather", "阴", "high", 27, "low", 20)
            )
        );
    }

    private double solarCurve(int slot, double capacityKw, Random rng, double factor) {
        double hour = slot * 0.25;
        if (hour < 6 || hour > 19) return 0;
        double peak = (hour - 6) / 6.5;
        double curve = Math.sin(peak * Math.PI) * capacityKw * factor * (0.85 + rng.nextDouble() * 0.15);
        return Math.max(0, curve);
    }

    private Map<String, Object> kpiItem(String key, String label, double value, String unit) {
        return orderedMap("key", key, "label", label, "value", value, "unit", unit);
    }

    private record StatusMeta(String label, String color) {
    }

    private record CapacityRange(double min, double max) {
    }
}
