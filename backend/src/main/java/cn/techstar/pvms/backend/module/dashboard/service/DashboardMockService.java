package cn.techstar.pvms.backend.module.dashboard.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

@Service
public class DashboardMockService {

    private static final List<StationRecord> STATIONS = List.of(
        new StationRecord("PV-001", "深圳港科园区光伏电站", "华南园区", 113.9489, 22.5408, "normal", 2500, 1823, 8432, 3372.8, 84.1, 3.37, "A", 99.2, "深圳港科生态园 8 栋屋面"),
        new StationRecord("PV-002", "松山湖智造园光伏电站", "华南园区", 113.8854, 22.9192, "warning", 6200, 3986, 20840, 8336.0, 82.3, 3.36, "A-", 98.1, "松山湖智造园 A 区厂房屋顶"),
        new StationRecord("PV-003", "武汉物流基地光伏电站", "华中园区", 114.3066, 30.5928, "fault", 4800, 2156, 13542, 5416.8, 76.8, 2.82, "B", 95.7, "武汉东西湖物流园 2 号仓屋顶"),
        new StationRecord("PV-004", "合肥研发中心光伏电站", "华东园区", 117.2272, 31.8206, "maintenance", 1800, 684, 2958, 1183.2, 71.9, 1.64, "B+", 92.4, "合肥研发中心 C 区车棚"),
        new StationRecord("PV-005", "天津港储能园光伏电站", "华北园区", 117.3616, 39.3434, "offline", 3200, 0, 1180, 472.0, 52.4, 0.37, "C", 81.1, "天津港储能园区堆场车棚"),
        new StationRecord("PV-006", "成都西部基地光伏电站", "西南园区", 104.0668, 30.5728, "normal", 5400, 3618, 17924, 7169.6, 83.5, 3.32, "A", 98.7, "成都西部基地生产楼及连廊")
    );

    private static final Map<String, StatusMeta> STATUS_META = Map.of(
        "normal", new StatusMeta("正常", "#67C23A"),
        "warning", new StatusMeta("告警", "#E6A23C"),
        "fault", new StatusMeta("故障", "#F56C6C"),
        "maintenance", new StatusMeta("检修", "#409EFF"),
        "offline", new StatusMeta("离线", "#909399")
    );

    private static final List<AlarmRecord> ALARMS = List.of(
        new AlarmRecord("ALM-20260322-001", "14:32", "critical", "紧急", "逆变器 INV-03", "PV-003", "武汉物流基地光伏电站", "dc-over-voltage", "直流侧过压告警，需立即复核组串电压与断路器状态。", "未处理", "华中运维一组", "建议 15 分钟内派单复核，并同步检查同列逆变器。"),
        new AlarmRecord("ALM-20260322-002", "14:18", "major", "重要", "箱变 T1", "PV-002", "松山湖智造园光伏电站", "transformer-high-temp", "箱变高压侧过温预警，温升超过基线 8.6°C。", "处理中", "华南运维中心", "核查散热风道和负荷分配，必要时切换低峰窗口检修。"),
        new AlarmRecord("ALM-20260322-003", "13:57", "minor", "一般", "汇流箱 HB-07", "PV-001", "深圳港科园区光伏电站", "low-current", "支路电流偏低，疑似单支路积灰或遮挡。", "待确认", "华南运维中心", "安排现场清扫，并对该支路做 IV 曲线复测。"),
        new AlarmRecord("ALM-20260322-004", "13:40", "hint", "提示", "气象站 WS-02", "PV-006", "成都西部基地光伏电站", "data-delay", "天气采样上报延迟 90 秒，已自动补采。", "未处理", "平台值班", "观察网络抖动是否持续，无需立刻派单。"),
        new AlarmRecord("ALM-20260322-005", "13:12", "major", "重要", "采集器 DTU-11", "PV-005", "天津港储能园光伏电站", "communication-lost", "采集器离线超过 15 分钟，关联设备数据中断。", "未处理", "华北运维组", "优先检查交换机与 4G 备链路，恢复后补传离线数据。")
    );

    private static final Map<String, WeatherProfileRecord> WEATHER = Map.of(
        "PV-001", new WeatherProfileRecord("深圳港科园区光伏电站", new CurrentWeatherRecord("多云", 28, 680, 65, 3.2, "东南风"), List.of(
            new ForecastWeatherRecord("03-23", "多云", 29, 23, 620),
            new ForecastWeatherRecord("03-24", "阵雨", 25, 21, 320),
            new ForecastWeatherRecord("03-25", "晴", 30, 24, 750)
        )),
        "PV-002", new WeatherProfileRecord("松山湖智造园光伏电站", new CurrentWeatherRecord("晴转云", 27, 702, 59, 2.7, "南风"), List.of(
            new ForecastWeatherRecord("03-23", "晴", 30, 22, 760),
            new ForecastWeatherRecord("03-24", "多云", 28, 22, 580),
            new ForecastWeatherRecord("03-25", "阵雨", 26, 21, 340)
        )),
        "PV-003", new WeatherProfileRecord("武汉物流基地光伏电站", new CurrentWeatherRecord("阴", 23, 412, 71, 2.1, "东北风"), List.of(
            new ForecastWeatherRecord("03-23", "多云", 25, 18, 520),
            new ForecastWeatherRecord("03-24", "小雨", 22, 17, 210),
            new ForecastWeatherRecord("03-25", "晴", 27, 18, 710)
        )),
        "PV-004", new WeatherProfileRecord("合肥研发中心光伏电站", new CurrentWeatherRecord("小雨", 21, 236, 78, 3.6, "北风"), List.of(
            new ForecastWeatherRecord("03-23", "阴", 24, 17, 300),
            new ForecastWeatherRecord("03-24", "多云", 26, 18, 560),
            new ForecastWeatherRecord("03-25", "晴", 29, 20, 740)
        )),
        "PV-005", new WeatherProfileRecord("天津港储能园光伏电站", new CurrentWeatherRecord("大风", 16, 458, 48, 7.3, "西北风"), List.of(
            new ForecastWeatherRecord("03-23", "晴", 18, 10, 610),
            new ForecastWeatherRecord("03-24", "晴", 19, 11, 640),
            new ForecastWeatherRecord("03-25", "多云", 17, 9, 470)
        )),
        "PV-006", new WeatherProfileRecord("成都西部基地光伏电站", new CurrentWeatherRecord("晴", 24, 648, 57, 2.4, "西南风"), List.of(
            new ForecastWeatherRecord("03-23", "晴", 27, 18, 700),
            new ForecastWeatherRecord("03-24", "多云", 25, 17, 540),
            new ForecastWeatherRecord("03-25", "阵雨", 22, 16, 280)
        ))
    );

    public Map<String, Object> getStationsGeo(String status, String region, String capacityRange) {
        List<StationRecord> stations = filterStations(status, region, capacityRange);

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
                    option("3MW 以下", "lt3000"),
                    option("3MW - 5MW", "3000to5000"),
                    option("5MW 以上", "gte5000")
                )
            ),
            "summary", STATUS_META.entrySet().stream()
                .map(entry -> orderedMap(
                    "key", entry.getKey(),
                    "label", entry.getValue().label(),
                    "color", entry.getValue().color(),
                    "count", (int) stations.stream().filter(item -> Objects.equals(item.status(), entry.getKey())).count()
                ))
                .toList(),
            "stations", stations.stream().map(this::mapStation).toList()
        );
    }

    public Map<String, Object> getKpiSummary(String stationId) {
        List<StationRecord> scope = resolveScope(stationId);
        double capacityKwp = sum(scope, StationRecord::capacityKwp);
        double realtimePowerKw = sum(scope, StationRecord::realtimePowerKw);
        double todayEnergyKwh = sum(scope, StationRecord::todayEnergyKwh);
        double todayRevenueCny = sum(scope, StationRecord::todayRevenueCny);
        double availability = average(scope, StationRecord::availability);
        double equivalentHours = average(scope, StationRecord::equivalentHours);
        double deviationRate = stationId == null || stationId.isBlank() ? 4.6 : 3.8;

        return orderedMap(
            "updatedAt", "2026-03-22 14:35",
            "focusLabel", resolveFocusLabel(stationId),
            "items", List.of(
                kpiItem("capacity", "总装机容量", round(capacityKwp / 1000, 1), "MWp", "el-icon-office-building", "teal", "园区集中式 + 分布式汇总", null, "稳定"),
                kpiItem("realtime", "实时总功率", round(realtimePowerKw / 1000, 2), "MW", "el-icon-lightning", "blue", "较 10 分钟前 +2.6%", 2.6, "较 10 分钟前"),
                kpiItem("todayEnergy", "当日发电量", round(todayEnergyKwh / 1000, 2), "MWh", "el-icon-data-analysis", "emerald", "较昨日 +8.3%", 8.3, "较昨日"),
                kpiItem("todayRevenue", "当日收益", round(todayRevenueCny / 10000, 2), "万元", "el-icon-coin", "orange", "较昨日 +6.8%", 6.8, "较昨日"),
                kpiItem("equivalentHours", "等效利用小时", round(equivalentHours, 2), "h", "el-icon-timer", "blue", "较去年 +0.14h", 5.1, "较去年"),
                kpiItem("availability", "设备在线率", round(availability, 1), "%", "el-icon-cpu", "teal", "在线设备健康度持续稳定", 0.8, "较昨日"),
                kpiItem("co2", "CO2 减排量", round(todayEnergyKwh * 0.000785, 1), "吨", "el-icon-s-opportunity", "emerald", "累计环境收益折算", null, "累计"),
                kpiItem("deviationRate", "偏差率", round(deviationRate, 1), "%", "el-icon-warning-outline", deviationRate > 4 ? "orange" : "blue", deviationRate > 4 ? "偏差临近上限，需重点关注" : "偏差控制在安全区间", -0.6, "较昨日")
            )
        );
    }

    public Map<String, Object> getPowerCurve(String stationId, String date) {
        StationRecord station = resolveStation(stationId);
        String currentDate = normalizeDate(date);
        List<Map<String, Object>> actual = buildCurveSeries(station, currentDate, "actual");
        List<Map<String, Object>> plan = buildCurveSeries(station, currentDate, "plan");
        List<Map<String, Object>> forecast = buildCurveSeries(station, currentDate, "forecast");
        List<Map<String, Object>> irradiance = buildCurveSeries(station, currentDate, "irradiance");
        double peakPower = actual.stream()
            .mapToDouble(item -> ((Number) item.get("value")).doubleValue())
            .max()
            .orElse(0);
        double planPeak = plan.stream()
            .mapToDouble(item -> ((Number) item.get("value")).doubleValue())
            .max()
            .orElse(1);

        return orderedMap(
            "stationId", station.id(),
            "stationName", station.name(),
            "currentDate", currentDate,
            "dateTabs", List.of(
                option("今日", "2026-03-22"),
                option("昨日", "2026-03-21")
            ),
            "deviationRate", round(((peakPower - planPeak) / planPeak) * 100, 1),
            "peakPowerKw", round(peakPower, 0),
            "actual", actual,
            "plan", plan,
            "forecast", forecast,
            "irradiance", irradiance
        );
    }

    public Map<String, Object> getStationRanking(String metric) {
        String currentMetric = switch (metric == null ? "" : metric) {
            case "hours", "pr" -> metric;
            default -> "energy";
        };

        List<Map<String, Object>> rankings = STATIONS.stream()
            .map(station -> orderedMap(
                "stationId", station.id(),
                "stationName", station.name(),
                "value", switch (currentMetric) {
                    case "hours" -> round(station.equivalentHours(), 1);
                    case "pr" -> round(station.pr(), 1);
                    default -> round(station.todayEnergyKwh(), 0);
                },
                "rankChange", switch (station.status()) {
                    case "normal" -> 1;
                    case "fault" -> -2;
                    default -> 0;
                }
            ))
            .sorted((left, right) -> Double.compare(
                ((Number) right.get("value")).doubleValue(),
                ((Number) left.get("value")).doubleValue()
            ))
            .toList();

        return orderedMap(
            "currentMetric", currentMetric,
            "metricOptions", List.of(
                option("日发电量", "energy"),
                option("等效小时", "hours"),
                option("PR 值", "pr")
            ),
            "unit", switch (currentMetric) {
                case "hours" -> "h";
                case "pr" -> "%";
                default -> "kWh";
            },
            "rankings", rankings
        );
    }

    public Map<String, Object> getRecentAlarms(String level, String stationId) {
        List<AlarmRecord> items = ALARMS.stream()
            .filter(item -> level == null || level.isBlank() || Objects.equals(item.level(), level))
            .filter(item -> stationId == null || stationId.isBlank() || Objects.equals(item.stationId(), stationId))
            .toList();

        return orderedMap(
            "summary", orderedMap(
                "critical", ALARMS.stream().filter(item -> Objects.equals(item.level(), "critical")).count(),
                "major", ALARMS.stream().filter(item -> Objects.equals(item.level(), "major")).count(),
                "minor", ALARMS.stream().filter(item -> Objects.equals(item.level(), "minor")).count(),
                "hint", ALARMS.stream().filter(item -> Objects.equals(item.level(), "hint")).count()
            ),
            "items", items.stream().map(this::mapAlarm).toList()
        );
    }

    public Map<String, Object> getWeather(String stationId) {
        StationRecord station = resolveStation(stationId);
        WeatherProfileRecord profile = WEATHER.get(station.id());

        return orderedMap(
            "stationId", station.id(),
            "stationName", profile.stationName(),
            "current", orderedMap(
                "weather", profile.current().weather(),
                "temperature", profile.current().temperature(),
                "irradiance", profile.current().irradiance(),
                "humidity", profile.current().humidity(),
                "windSpeed", profile.current().windSpeed(),
                "windDirection", profile.current().windDirection()
            ),
            "forecast", profile.forecast().stream()
                .map(item -> orderedMap(
                    "date", item.date(),
                    "weather", item.weather(),
                    "tempHigh", item.tempHigh(),
                    "tempLow", item.tempLow(),
                    "irradianceEstimate", item.irradianceEstimate()
                ))
                .toList()
        );
    }

    private List<StationRecord> filterStations(String status, String region, String capacityRange) {
        CapacityRange range = parseCapacityRange(capacityRange);
        return STATIONS.stream()
            .filter(item -> status == null || status.isBlank() || Objects.equals(item.status(), status))
            .filter(item -> region == null || region.isBlank() || Objects.equals(item.region(), region))
            .filter(item -> item.capacityKwp() >= range.min() && item.capacityKwp() <= range.max())
            .toList();
    }

    private List<StationRecord> resolveScope(String stationId) {
        if (stationId == null || stationId.isBlank()) {
            return STATIONS;
        }
        return STATIONS.stream()
            .filter(item -> Objects.equals(item.id(), stationId))
            .toList();
    }

    private StationRecord resolveStation(String stationId) {
        if (stationId != null && !stationId.isBlank()) {
            return STATIONS.stream()
                .filter(item -> Objects.equals(item.id(), stationId))
                .findFirst()
                .orElse(STATIONS.getFirst());
        }
        return STATIONS.getFirst();
    }

    private String resolveFocusLabel(String stationId) {
        return stationId == null || stationId.isBlank()
            ? "全系统"
            : resolveStation(stationId).name();
    }

    private String normalizeDate(String date) {
        if (date == null || date.isBlank()) {
            return "2026-03-22";
        }
        if ("today".equals(date)) {
            return "2026-03-22";
        }
        if ("yesterday".equals(date)) {
            return "2026-03-21";
        }
        return LocalDate.parse(date).toString();
    }

    private List<Map<String, Object>> buildCurveSeries(StationRecord station, String date, String seriesKey) {
        List<Map<String, Object>> points = new ArrayList<>();
        double capacityMw = station.capacityKwp() / 1000.0;
        double statusFactor = switch (station.status()) {
            case "offline" -> 0.05;
            case "fault" -> 0.62;
            default -> 0.9;
        };

        for (int index = 0; index < 96; index += 1) {
            double hour = index / 4.0;
            double solarWindow = Math.max(0, Math.sin(((hour - 6) / 12) * Math.PI));
            double actualBase = capacityMw * 980 * solarWindow * statusFactor;
            double actual = round(actualBase + Math.cos(index / 8.0) * 22, 0);
            double plan = round(actualBase * 1.06 + 35, 0);
            double forecast = round(actualBase * 1.02 + Math.sin(index / 6.0) * 18, 0);
            double irradiance = round(actualBase * 1.8 + solarWindow * 160, 0);

            String hourText = String.format("%02d", (int) hour);
            String minuteText = String.format("%02d", (index % 4) * 15);
            points.add(orderedMap(
                "time", date + " " + hourText + ":" + minuteText + ":00",
                "value", Math.max(0, switch (seriesKey) {
                    case "plan" -> plan;
                    case "forecast" -> forecast;
                    case "irradiance" -> irradiance;
                    default -> actual;
                })
            ));
        }

        return points;
    }

    private List<Map<String, Object>> buildRegionOptions() {
        List<Map<String, Object>> options = new ArrayList<>();
        options.add(option("全部区域", ""));
        STATIONS.stream()
            .map(StationRecord::region)
            .distinct()
            .forEach(region -> options.add(option(region, region)));
        return options;
    }

    private Map<String, Object> mapStation(StationRecord station) {
        StatusMeta meta = STATUS_META.get(station.status());
        return orderedMap(
            "id", station.id(),
            "name", station.name(),
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

    private Map<String, Object> mapAlarm(AlarmRecord alarm) {
        return orderedMap(
            "id", alarm.id(),
            "time", alarm.time(),
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

    private Map<String, Object> option(String label, String value) {
        return orderedMap("label", label, "value", value);
    }

    private Map<String, Object> kpiItem(
        String key,
        String title,
        Double value,
        String unit,
        String icon,
        String accent,
        String helper,
        Double changeRate,
        String changeLabel
    ) {
        return orderedMap(
            "key", key,
            "title", title,
            "value", value,
            "unit", unit,
            "icon", icon,
            "accent", accent,
            "helper", helper,
            "changeRate", changeRate,
            "changeLabel", changeLabel
        );
    }

    private Map<String, Object> orderedMap(Object... entries) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            result.put((String) entries[index], entries[index + 1]);
        }
        return result;
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

    private double sum(List<StationRecord> scope, ToDoubleFunction<StationRecord> extractor) {
        return scope.stream().mapToDouble(extractor).sum();
    }

    private double average(List<StationRecord> scope, ToDoubleFunction<StationRecord> extractor) {
        return round(scope.stream().mapToDouble(extractor).average().orElse(0), 2);
    }

    private double round(double value, int scale) {
        double factor = Math.pow(10, scale);
        return Math.round(value * factor) / factor;
    }

    private record StatusMeta(String label, String color) {
    }

    private record CapacityRange(double min, double max) {
    }

    private record StationRecord(
        String id,
        String name,
        String region,
        double longitude,
        double latitude,
        String status,
        double capacityKwp,
        double realtimePowerKw,
        double todayEnergyKwh,
        double todayRevenueCny,
        double pr,
        double equivalentHours,
        String healthGrade,
        double availability,
        String address
    ) {
    }

    private record AlarmRecord(
        String id,
        String time,
        String level,
        String levelLabel,
        String deviceName,
        String stationId,
        String stationName,
        String alarmType,
        String description,
        String status,
        String owner,
        String suggestion
    ) {
    }

    private record WeatherProfileRecord(
        String stationName,
        CurrentWeatherRecord current,
        List<ForecastWeatherRecord> forecast
    ) {
    }

    private record CurrentWeatherRecord(
        String weather,
        int temperature,
        int irradiance,
        int humidity,
        double windSpeed,
        String windDirection
    ) {
    }

    private record ForecastWeatherRecord(
        String date,
        String weather,
        int tempHigh,
        int tempLow,
        int irradianceEstimate
    ) {
    }
}
