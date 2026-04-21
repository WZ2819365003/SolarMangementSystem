package cn.techstar.pvms.backend.module.stations.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class StationMockService {

    private static final Map<String, StatusMeta> STATUS_META = Map.of(
        "normal", new StatusMeta("正常", "#67C23A"),
        "warning", new StatusMeta("告警", "#E6A23C"),
        "fault", new StatusMeta("故障", "#F56C6C"),
        "maintenance", new StatusMeta("检修", "#409EFF"),
        "offline", new StatusMeta("离线", "#909399")
    );

    private static final List<ResourceUnitRecord> RESOURCE_UNITS = List.of(
        new ResourceUnitRecord(
            "RU-001",
            "华南工商业聚合单元 A",
            "华南区域",
            "normal",
            6,
            5.2,
            3.86,
            25.4,
            0.82,
            1.36,
            97.8,
            95.2,
            -2.4,
            2,
            "多云",
            28,
            680,
            65,
            3.2,
            "日前策略 + 实时修正",
            "以工商业屋顶光伏为主",
            "午后具备稳定上调空间",
            new DispatchSummary(14, 13, 92.9, 96, "low", "低风险"),
            new AlarmSummary(4, 1, 2, 1, "松山湖园区电站预测偏差超阈值", "2026-03-23 14:10:00"),
            List.of(
                new MemberStationRecord("PV-002", "松山湖智造园光伏电站", "warning", 3986, 94.2, 2, "晴", 27),
                new MemberStationRecord("PV-001", "深圳湾科技园光伏电站", "normal", 1823, 99.2, 1, "多云", 28),
                new MemberStationRecord("PV-006", "成都西部基地光伏电站", "normal", 3618, 98.7, 0, "晴", 24)
            )
        ),
        new ResourceUnitRecord(
            "RU-002",
            "华南园区快响应单元 B",
            "华南区域",
            "normal",
            4,
            4.1,
            2.94,
            18.7,
            0.63,
            1.02,
            96.5,
            94.6,
            1.8,
            1,
            "晴",
            29,
            720,
            61,
            2.8,
            "日内滚动修正",
            "以园区负荷配合型资源为主",
            "当前适合维持平稳跟踪",
            new DispatchSummary(11, 11, 100.0, 88, "low", "低风险"),
            new AlarmSummary(2, 0, 1, 1, "园区通信链路存在短时抖动", "2026-03-23 13:36:00"),
            List.of(
                new MemberStationRecord("PV-010", "广州科创园屋顶光伏", "normal", 1260, 97.1, 0, "晴", 29),
                new MemberStationRecord("PV-011", "佛山制造园储光单元", "normal", 968, 95.8, 1, "晴", 30),
                new MemberStationRecord("PV-012", "珠海冷站协同资源", "normal", 712, 96.4, 0, "晴", 28)
            )
        ),
        new ResourceUnitRecord(
            "RU-003",
            "华中物流园聚合单元",
            "华中区域",
            "fault",
            5,
            3.8,
            1.96,
            13.9,
            0.34,
            0.78,
            86.4,
            87.3,
            -9.1,
            5,
            "阴",
            24,
            410,
            73,
            2.1,
            "故障压降策略",
            "以物流园与仓储屋顶资源为主",
            "建议限制上调指令，先压缩偏差风险",
            new DispatchSummary(9, 6, 66.7, 182, "high", "高风险"),
            new AlarmSummary(6, 2, 2, 2, "武汉物流园资源单元响应超时", "2026-03-23 14:02:00"),
            List.of(
                new MemberStationRecord("PV-021", "武汉物流基地光伏电站", "fault", 1126, 82.5, 3, "阴", 24),
                new MemberStationRecord("PV-022", "襄阳分拨中心屋顶光伏", "warning", 486, 89.7, 1, "阴", 23),
                new MemberStationRecord("PV-023", "长沙仓储协同单元", "normal", 348, 93.2, 1, "多云", 26)
            )
        ),
        new ResourceUnitRecord(
            "RU-004",
            "华东制造园区聚合单元",
            "华东区域",
            "warning",
            7,
            6.4,
            4.22,
            29.3,
            0.96,
            1.58,
            93.6,
            91.8,
            4.3,
            3,
            "晴",
            27,
            705,
            57,
            3.1,
            "日前主跟踪 + 实时纠偏",
            "以制造园区与办公园区混合资源为主",
            "偏差抬头，建议缩短滚动预测窗口",
            new DispatchSummary(15, 13, 86.7, 104, "medium", "中风险"),
            new AlarmSummary(3, 0, 2, 1, "华东单元预测偏差连续两窗超阈值", "2026-03-23 13:48:00"),
            List.of(
                new MemberStationRecord("PV-031", "合肥研发中心光伏电站", "warning", 684, 92.4, 1, "小雨", 21),
                new MemberStationRecord("PV-032", "苏州精密制造园屋顶光伏", "normal", 1436, 95.6, 0, "晴", 27),
                new MemberStationRecord("PV-033", "嘉兴工业园柔性调节资源", "warning", 1198, 91.2, 2, "晴", 26)
            )
        ),
        new ResourceUnitRecord(
            "RU-005",
            "华北港储协同单元",
            "华北区域",
            "offline",
            3,
            2.7,
            0.18,
            2.1,
            0.10,
            0.42,
            68.9,
            73.4,
            -14.6,
            4,
            "大风",
            16,
            458,
            51,
            5.4,
            "限发保底策略",
            "以港储联动资源和边缘站点为主",
            "当前仅保留最小可调能力",
            new DispatchSummary(8, 5, 62.5, 210, "high", "高风险"),
            new AlarmSummary(5, 2, 2, 1, "港储单元多站点离线", "2026-03-23 13:12:00"),
            List.of(
                new MemberStationRecord("PV-041", "天津港储能园光伏电站", "offline", 0, 61.5, 3, "大风", 16),
                new MemberStationRecord("PV-042", "唐山物流园柔性资源", "warning", 96, 74.2, 1, "多云", 17),
                new MemberStationRecord("PV-043", "沧州港辅站屋顶光伏", "offline", 0, 70.8, 0, "大风", 15)
            )
        ),
        new ResourceUnitRecord(
            "RU-006",
            "西南柔性调节单元",
            "西南区域",
            "normal",
            6,
            5.9,
            4.08,
            27.8,
            0.88,
            1.34,
            98.1,
            96.1,
            -1.6,
            1,
            "晴",
            24,
            648,
            58,
            2.6,
            "柔性跟踪优先",
            "以山地园区和西部基地资源为主",
            "当前适合承接短时上调指令",
            new DispatchSummary(16, 15, 93.8, 84, "low", "低风险"),
            new AlarmSummary(1, 0, 0, 1, "单站通信重连后待人工复核", "2026-03-23 12:55:00"),
            List.of(
                new MemberStationRecord("PV-051", "成都西部基地光伏电站", "normal", 3618, 98.7, 0, "晴", 24),
                new MemberStationRecord("PV-052", "重庆园区协同资源", "normal", 972, 97.9, 1, "多云", 25),
                new MemberStationRecord("PV-053", "昆明智能仓光伏单元", "normal", 684, 97.6, 0, "晴", 23)
            )
        )
    );

    public Map<String, Object> getResourceUnitList(
        String keyword,
        String status,
        String region,
        String capacityRange,
        int page,
        int pageSize
    ) {
        List<ResourceUnitRecord> filtered = filterResourceUnits(keyword, status, region, capacityRange);
        int normalizedPage = Math.max(page, 1);
        int normalizedPageSize = Math.max(pageSize, 1);
        int fromIndex = Math.min((normalizedPage - 1) * normalizedPageSize, filtered.size());
        int toIndex = Math.min(fromIndex + normalizedPageSize, filtered.size());
        List<Map<String, Object>> pageList = filtered.subList(fromIndex, toIndex).stream()
            .map(this::mapResourceUnitListItem)
            .toList();

        int stationTotal = filtered.stream().mapToInt(ResourceUnitRecord::stationCount).sum();
        double dispatchableCapacity = filtered.stream().mapToDouble(ResourceUnitRecord::dispatchableCapacityMw).sum();
        double averageOnlineRate = filtered.stream()
            .mapToDouble(ResourceUnitRecord::onlineRate)
            .average()
            .orElse(0);

        return orderedMap(
            "filters", orderedMap(
                "statusOptions", List.of(
                    option("全部状态", ""),
                    option("正常", "normal"),
                    option("告警", "warning"),
                    option("故障", "fault"),
                    option("检修", "maintenance"),
                    option("离线", "offline")
                ),
                "regionOptions", buildRegionOptions(),
                "capacityOptions", List.of(
                    option("全部容量", ""),
                    option("3MW 以下", "lt3"),
                    option("3MW - 6MW", "3to6"),
                    option("6MW 以上", "gte6")
                )
            ),
            "statistics", List.of(
                statistic("unitTotal", "资源单元总数", filtered.size(), "个", "el-icon-s-operation"),
                statistic("stationTotal", "纳管电站数", stationTotal, "座", "el-icon-office-building"),
                statistic("dispatchableCapacity", "可调总容量", round(dispatchableCapacity, 1), "MW", "el-icon-data-analysis"),
                statistic("onlineRate", "平均在线率", round(averageOnlineRate, 1), "%", "el-icon-success")
            ),
            "total", filtered.size(),
            "page", normalizedPage,
            "pageSize", normalizedPageSize,
            "list", pageList
        );
    }

    public Map<String, Object> getResourceUnitOverview(String resourceUnitId) {
        ResourceUnitRecord resourceUnit = resolveResourceUnit(resourceUnitId);
        StatusMeta statusMeta = STATUS_META.get(resourceUnit.status());

        return orderedMap(
            "info", orderedMap(
                "id", resourceUnit.id(),
                "name", resourceUnit.name(),
                "region", resourceUnit.region(),
                "status", resourceUnit.status(),
                "statusLabel", statusMeta.label(),
                "statusColor", statusMeta.color(),
                "stationCount", resourceUnit.stationCount(),
                "dispatchableCapacityMw", resourceUnit.dispatchableCapacityMw(),
                "dispatchMode", resourceUnit.dispatchMode(),
                "strategyLabel", resourceUnit.strategyLabel()
            ),
            "kpi", orderedMap(
                "realtimePowerMw", resourceUnit.realtimePowerMw(),
                "todayEnergyMwh", resourceUnit.todayEnergyMwh(),
                "upRegulationMw", resourceUnit.upRegulationMw(),
                "downRegulationMw", resourceUnit.downRegulationMw(),
                "onlineRate", resourceUnit.onlineRate(),
                "forecastAccuracy", resourceUnit.forecastAccuracy()
            ),
            "weather", orderedMap(
                "weather", resourceUnit.weather(),
                "temperature", resourceUnit.temperature(),
                "irradiance", resourceUnit.irradiance(),
                "humidity", resourceUnit.humidity(),
                "windSpeed", resourceUnit.windSpeed(),
                "suggestion", resourceUnit.weatherSuggestion()
            ),
            "dispatchSummary", orderedMap(
                "issuedCount", resourceUnit.dispatchSummary().issuedCount(),
                "successCount", resourceUnit.dispatchSummary().successCount(),
                "successRate", resourceUnit.dispatchSummary().successRate(),
                "lastResponseSeconds", resourceUnit.dispatchSummary().lastResponseSeconds(),
                "riskLevel", resourceUnit.dispatchSummary().riskLevel(),
                "riskLabel", resourceUnit.dispatchSummary().riskLabel()
            ),
            "alarmSummary", orderedMap(
                "total", resourceUnit.alarmSummary().total(),
                "critical", resourceUnit.alarmSummary().critical(),
                "major", resourceUnit.alarmSummary().major(),
                "minor", resourceUnit.alarmSummary().minor(),
                "latestTitle", resourceUnit.alarmSummary().latestTitle(),
                "latestTime", resourceUnit.alarmSummary().latestTime()
            ),
            "memberStations", resourceUnit.memberStations().stream().map(this::mapMemberStation).toList()
        );
    }

    public Map<String, Object> getResourceUnitPowerCurve(String resourceUnitId, String date) {
        ResourceUnitRecord resourceUnit = resolveResourceUnit(resourceUnitId);
        String currentDate = normalizeDate(date);
        List<Map<String, Object>> actual = buildCurveSeries(resourceUnit, currentDate, "actual");
        List<Map<String, Object>> forecast = buildCurveSeries(resourceUnit, currentDate, "forecast");
        List<Map<String, Object>> baseline = buildCurveSeries(resourceUnit, currentDate, "baseline");
        List<Map<String, Object>> irradiance = buildCurveSeries(resourceUnit, currentDate, "irradiance");
        double peakPower = actual.stream()
            .mapToDouble(item -> ((Number) item.get("value")).doubleValue())
            .max()
            .orElse(0);
        double forecastPeak = forecast.stream()
            .mapToDouble(item -> ((Number) item.get("value")).doubleValue())
            .max()
            .orElse(1);

        return orderedMap(
            "resourceUnitId", resourceUnit.id(),
            "resourceUnitName", resourceUnit.name(),
            "currentDate", currentDate,
            "peakPowerMw", round(peakPower, 2),
            "deviationRate", round(((peakPower - forecastPeak) / forecastPeak) * 100, 1),
            "actual", actual,
            "forecast", forecast,
            "baseline", baseline,
            "irradiance", irradiance
        );
    }

    private List<ResourceUnitRecord> filterResourceUnits(String keyword, String status, String region, String capacityRange) {
        CapacityRange range = parseCapacityRange(capacityRange);
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase();

        return RESOURCE_UNITS.stream()
            .filter(item -> normalizedKeyword.isBlank()
                || item.name().toLowerCase().contains(normalizedKeyword)
                || item.region().toLowerCase().contains(normalizedKeyword)
                || item.strategyLabel().toLowerCase().contains(normalizedKeyword))
            .filter(item -> status == null || status.isBlank() || Objects.equals(item.status(), status))
            .filter(item -> region == null || region.isBlank() || Objects.equals(item.region(), region))
            .filter(item -> item.dispatchableCapacityMw() >= range.min() && item.dispatchableCapacityMw() <= range.max())
            .toList();
    }

    private CapacityRange parseCapacityRange(String capacityRange) {
        return switch (capacityRange == null ? "" : capacityRange) {
            case "lt3" -> new CapacityRange(0, 3);
            case "3to6" -> new CapacityRange(3, 6);
            case "gte6" -> new CapacityRange(6, Double.MAX_VALUE);
            default -> new CapacityRange(0, Double.MAX_VALUE);
        };
    }

    private List<Map<String, Object>> buildRegionOptions() {
        return RESOURCE_UNITS.stream()
            .map(ResourceUnitRecord::region)
            .distinct()
            .map(region -> option(region, region))
            .collect(java.util.stream.Collectors.collectingAndThen(
                java.util.stream.Collectors.toList(),
                items -> {
                    items.add(0, option("全部区域", ""));
                    return items;
                }
            ));
    }

    private Map<String, Object> mapResourceUnitListItem(ResourceUnitRecord resourceUnit) {
        StatusMeta statusMeta = STATUS_META.get(resourceUnit.status());
        return orderedMap(
            "id", resourceUnit.id(),
            "name", resourceUnit.name(),
            "region", resourceUnit.region(),
            "status", resourceUnit.status(),
            "statusLabel", statusMeta.label(),
            "statusColor", statusMeta.color(),
            "stationCount", resourceUnit.stationCount(),
            "dispatchableCapacityMw", resourceUnit.dispatchableCapacityMw(),
            "realtimePowerMw", resourceUnit.realtimePowerMw(),
            "forecastDeviationRate", resourceUnit.forecastDeviationRate(),
            "onlineRate", resourceUnit.onlineRate(),
            "alarmCount", resourceUnit.alarmCount(),
            "weather", orderedMap(
                "weather", resourceUnit.weather(),
                "temperature", resourceUnit.temperature(),
                "irradiance", resourceUnit.irradiance()
            )
        );
    }

    private Map<String, Object> mapMemberStation(MemberStationRecord station) {
        StatusMeta statusMeta = STATUS_META.get(station.status());
        return orderedMap(
            "id", station.id(),
            "name", station.name(),
            "status", station.status(),
            "statusLabel", statusMeta.label(),
            "realtimePowerKw", station.realtimePowerKw(),
            "onlineRate", station.onlineRate(),
            "alarmCount", station.alarmCount(),
            "weather", orderedMap(
                "weather", station.weather(),
                "temperature", station.temperature()
            )
        );
    }

    private List<Map<String, Object>> buildCurveSeries(ResourceUnitRecord resourceUnit, String date, String seriesKey) {
        double availabilityFactor = switch (resourceUnit.status()) {
            case "offline" -> 0.18;
            case "fault" -> 0.58;
            case "maintenance" -> 0.42;
            case "warning" -> 0.84;
            default -> 0.93;
        };

        return java.util.stream.IntStream.range(0, 96)
            .mapToObj(index -> {
                double hour = index / 4.0;
                double solarWindow = Math.max(0, Math.sin(((hour - 6) / 12) * Math.PI));
                double dispatchableBase = resourceUnit.dispatchableCapacityMw() * solarWindow;
                double rawValue = switch (seriesKey) {
                    case "forecast" -> dispatchableBase * (availabilityFactor + 0.08) + Math.sin(index / 10.0) * 0.08;
                    case "baseline" -> dispatchableBase * (availabilityFactor + 0.03) + 0.12;
                    case "irradiance" -> (dispatchableBase * 120) + solarWindow * 80;
                    default -> dispatchableBase * availabilityFactor + Math.cos(index / 8.0) * 0.06;
                };

                int hourPart = index / 4;
                int minutePart = (index % 4) * 15;

                return orderedMap(
                    "time", String.format("%s %02d:%02d:00", date, hourPart, minutePart),
                    "value", round(Math.max(0, rawValue), "irradiance".equals(seriesKey) ? 0 : 2)
                );
            })
            .toList();
    }

    private ResourceUnitRecord resolveResourceUnit(String resourceUnitId) {
        return RESOURCE_UNITS.stream()
            .filter(item -> Objects.equals(item.id(), resourceUnitId))
            .findFirst()
            .orElse(RESOURCE_UNITS.getFirst());
    }

    private String normalizeDate(String date) {
        if (date == null || date.isBlank()) {
            return LocalDate.of(2026, 3, 23).toString();
        }
        return date;
    }

    private Map<String, Object> statistic(String key, String label, Object value, String unit, String icon) {
        return orderedMap(
            "key", key,
            "label", label,
            "value", value,
            "unit", unit,
            "icon", icon
        );
    }

    private Map<String, Object> option(String label, String value) {
        return orderedMap(
            "label", label,
            "value", value
        );
    }

    private double round(double value, int digits) {
        double scale = Math.pow(10, digits);
        return Math.round(value * scale) / scale;
    }

    private Map<String, Object> orderedMap(Object... values) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int index = 0; index < values.length; index += 2) {
            result.put((String) values[index], values[index + 1]);
        }
        return result;
    }

    private record StatusMeta(String label, String color) {
    }

    private record CapacityRange(double min, double max) {
    }

    private record DispatchSummary(
        int issuedCount,
        int successCount,
        double successRate,
        int lastResponseSeconds,
        String riskLevel,
        String riskLabel
    ) {
    }

    private record AlarmSummary(
        int total,
        int critical,
        int major,
        int minor,
        String latestTitle,
        String latestTime
    ) {
    }

    private record MemberStationRecord(
        String id,
        String name,
        String status,
        double realtimePowerKw,
        double onlineRate,
        int alarmCount,
        String weather,
        int temperature
    ) {
    }

    private record ResourceUnitRecord(
        String id,
        String name,
        String region,
        String status,
        int stationCount,
        double dispatchableCapacityMw,
        double realtimePowerMw,
        double todayEnergyMwh,
        double upRegulationMw,
        double downRegulationMw,
        double onlineRate,
        double forecastAccuracy,
        double forecastDeviationRate,
        int alarmCount,
        String weather,
        int temperature,
        int irradiance,
        int humidity,
        double windSpeed,
        String dispatchMode,
        String strategyLabel,
        String weatherSuggestion,
        DispatchSummary dispatchSummary,
        AlarmSummary alarmSummary,
        List<MemberStationRecord> memberStations
    ) {
    }
}
