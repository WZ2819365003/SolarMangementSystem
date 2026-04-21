package cn.techstar.pvms.backend.module.productionmonitor.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

@Service
public class ProductionMonitorMockService {

    private static final Map<String, StatusMeta> STATUS_META = Map.of(
        "normal", new StatusMeta("正常", "#67C23A"),
        "warning", new StatusMeta("告警", "#E6A23C"),
        "fault", new StatusMeta("故障", "#F56C6C"),
        "offline", new StatusMeta("离线", "#909399")
    );

    private static final Map<String, Double> STATION_STATUS_FACTOR = Map.of(
        "normal", 1.0,
        "warning", 0.88,
        "fault", 0.52,
        "offline", 0.18
    );

    private static final List<ResourceUnitRecord> RESOURCE_UNITS = List.of(
        new ResourceUnitRecord(
            "RU-001", "深圳南山工商业聚合单元", "华南区域", "深圳", "normal", 12, 4,
            5.8, 4.18, 27.6, 0.92, 1.44, 98.4, 95.6, -2.1,
            "日前基线 + 实时修正", "同城工商业屋顶光伏聚合，成员站共享深圳南山天气剖面",
            new WeatherProfile("多云", "中云", 29, 746, 61, 3.1, "午后辐照条件稳定，适合维持上调空间"),
            new AlarmBrief(3, 0, 2, 1, "蛇口仓储园 B 站采集延迟导致偏差连续两个窗超阈值", "2026-03-23 14:18:00"),
            List.of(
                new MemberStationRecord("PV-001", "南山智造园 A 站", 1.9, 0.34, "normal", 99.1, 0),
                new MemberStationRecord("PV-002", "蛇口仓储园 B 站", 1.6, 0.28, "warning", 96.8, 1),
                new MemberStationRecord("PV-003", "科技生态园 C 站", 1.2, 0.22, "normal", 98.9, 0),
                new MemberStationRecord("PV-004", "前海冷站屋顶站", 1.1, 0.16, "normal", 98.5, 0)
            )
        ),
        new ResourceUnitRecord(
            "RU-002", "广州黄埔园区聚合单元", "华南区域", "广州", "warning", 15, 3,
            4.6, 3.04, 19.8, 0.56, 1.12, 94.9, 92.8, 4.7,
            "滚动预测 + 偏差压缩", "园区站群共享黄埔天气趋势，按负荷权重分摊出力",
            new WeatherProfile("晴转多云", "低云", 30, 692, 58, 2.8, "16:00 后云量抬升，预计实际出力略低于预测"),
            new AlarmBrief(4, 0, 2, 2, "黄埔制造园 B 站在 13:45 后出现预测偏差持续放大", "2026-03-23 14:06:00"),
            List.of(
                new MemberStationRecord("PV-011", "黄埔研发园 A 站", 1.7, 0.38, "normal", 97.1, 0),
                new MemberStationRecord("PV-012", "黄埔制造园 B 站", 1.5, 0.34, "warning", 93.2, 2),
                new MemberStationRecord("PV-013", "黄埔物流园 C 站", 1.4, 0.28, "normal", 94.4, 1)
            )
        ),
        new ResourceUnitRecord(
            "RU-003", "苏州园区制造聚合单元", "华东区域", "苏州", "normal", 11, 4,
            6.3, 4.56, 31.4, 1.06, 1.52, 97.5, 96.2, -1.4,
            "日前跟踪 + 实时纠偏", "同城制造园区资源聚合，天气趋势统一建模",
            new WeatherProfile("晴", "少云", 27, 782, 54, 2.4, "辐照度高且稳定，当前具备较好上调承接条件"),
            new AlarmBrief(2, 0, 1, 1, "昆山配套园 C 站通信抖动导致部分数据上送延迟", "2026-03-23 13:40:00"),
            List.of(
                new MemberStationRecord("PV-021", "苏州制造园 A 站", 2.0, 0.31, "normal", 98.3, 0),
                new MemberStationRecord("PV-022", "苏州精工园 B 站", 1.7, 0.27, "normal", 97.6, 0),
                new MemberStationRecord("PV-023", "昆山配套园 C 站", 1.4, 0.23, "warning", 95.8, 1),
                new MemberStationRecord("PV-024", "吴江仓储园 D 站", 1.2, 0.19, "normal", 97.9, 0)
            )
        ),
        new ResourceUnitRecord(
            "RU-004", "武汉物流协同聚合单元", "华中区域", "武汉", "fault", 18, 3,
            3.9, 1.82, 13.4, 0.22, 0.74, 86.2, 84.3, -9.6,
            "偏差压降", "物流园与仓储屋顶站点共享武汉天气剖面，但故障站拖低可调能力",
            new WeatherProfile("阴", "厚云", 23, 402, 72, 2.1, "云层持续偏厚，建议限制上调并优先压缩偏差风险"),
            new AlarmBrief(6, 2, 2, 2, "武汉物流园 A 站逆变器簇故障导致资源单元响应不完整", "2026-03-23 14:02:00"),
            List.of(
                new MemberStationRecord("PV-031", "武汉物流园 A 站", 1.5, 0.41, "fault", 82.4, 3),
                new MemberStationRecord("PV-032", "武汉仓储园 B 站", 1.3, 0.33, "warning", 88.9, 1),
                new MemberStationRecord("PV-033", "鄂州协同园 C 站", 1.1, 0.26, "normal", 91.6, 1)
            )
        )
    );

    public Map<String, Object> getMeta() {
        return orderedMap(
            "defaultResourceUnitId", RESOURCE_UNITS.getFirst().id(),
            "regionOptions", buildRegionOptions(),
            "resourceUnits", RESOURCE_UNITS.stream().map(this::mapMetaUnit).toList()
        );
    }

    public Map<String, Object> getOverview(String resourceUnitId) {
        ResourceUnitRecord unit = resolveResourceUnit(resourceUnitId);

        return orderedMap(
            "info", mapMetaUnit(unit),
            "kpis", List.of(
                metric("realtime", "实时总出力", unit.realtimePowerMw(), "MW", "当前资源单元聚合总出力", "el-icon-lightning", "blue"),
                metric("today-energy", "当日累计电量", unit.todayEnergyMwh(), "MWh", "当前日内累计电量", "el-icon-data-analysis", "emerald"),
                metric("up", "上调可调能力", unit.upRegulationMw(), "MW", "当前可承接上调空间", "el-icon-top", "teal"),
                metric("down", "下调可调能力", unit.downRegulationMw(), "MW", "当前可承接下调空间", "el-icon-bottom", "orange"),
                metric("online", "在线率", unit.onlineRate(), "%", "按成员电站在线质量聚合", "el-icon-success", "teal"),
                metric("accuracy", "预测准确率", unit.forecastAccuracy(), "%", "同区位天气下的资源单元预测命中率", "el-icon-s-marketing", "blue")
            ),
            "memberStations", buildMemberStations(unit),
            "weatherBrief", mapWeather(unit.weather()),
            "alarmBrief", mapAlarm(unit.alarm()),
            "summaryTable", List.of(
                overviewRow("09:00", round(unit.realtimePowerMw() * 0.74, 2), round(unit.dispatchableCapacityMw() * 0.86, 2), round(unit.onlineRate() - 0.6, 1), round(unit.forecastDeviationRate() + 1.1, 1), unit.weather().conclusion()),
                overviewRow("12:00", round(unit.realtimePowerMw() * 0.96, 2), round(unit.dispatchableCapacityMw() * 0.94, 2), unit.onlineRate(), unit.forecastDeviationRate(), unit.weather().conclusion()),
                overviewRow("15:00", unit.realtimePowerMw(), unit.dispatchableCapacityMw(), unit.onlineRate(), round(unit.forecastDeviationRate() - 0.5, 1), unit.weather().conclusion())
            )
        );
    }

    public Map<String, Object> getOutput(String resourceUnitId, String granularity) {
        ResourceUnitRecord unit = resolveResourceUnit(resourceUnitId);
        CurveData curveData = buildCurve(unit, granularity);
        List<Map<String, Object>> memberStations = buildMemberStations(unit);

        return orderedMap(
            "summary", List.of(
                metric("peak", "峰值出力", round(curveData.actual().stream().mapToDouble(Double::doubleValue).max().orElse(0), 2), "MW", "按当前粒度计算的峰值", "el-icon-data-line", "blue"),
                metric("deviation", "预测偏差率", unit.forecastDeviationRate(), "%", "资源单元整体偏差", "el-icon-warning-outline", "orange"),
                metric("irradiance", "峰值辐照度", curveData.irradiance().stream().mapToInt(Integer::intValue).max().orElse(0), "W/m²", "同区位共享气象趋势", "el-icon-s-opportunity", "teal"),
                metric("granularity", "当前粒度", granularity.replace("m", ""), "min", "切换粒度后曲线和表格联动", "el-icon-time", "emerald")
            ),
            "curve", orderedMap(
                "axis", curveData.axis(),
                "actual", curveData.actual(),
                "forecast", curveData.forecast(),
                "baseline", curveData.baseline(),
                "irradiance", curveData.irradiance(),
                "powerUnit", "MW",
                "weatherUnit", "W/m²"
            ),
            "weatherTrend", orderedMap(
                "axis", curveData.axis(),
                "series", List.of(
                    orderedMap("name", "辐照度", "data", curveData.irradiance(), "type", "bar", "yAxisIndex", 1, "color", "rgba(130, 208, 255, 0.35)"),
                    orderedMap("name", "温度", "data", curveData.temperature(), "color", "#00b578")
                )
            ),
            "contributionRanking", memberStations.stream()
                .sorted((prev, next) -> Double.compare(
                    ((Number) next.get("realtimePowerMw")).doubleValue(),
                    ((Number) prev.get("realtimePowerMw")).doubleValue()
                ))
                .map(item -> orderedMap(
                    "id", item.get("id"),
                    "name", item.get("name"),
                    "realtimePowerMw", item.get("realtimePowerMw"),
                    "shareRate", round(((Number) item.get("weight")).doubleValue() * 100, 0),
                    "weatherText", item.get("weatherText")
                ))
                .toList(),
            "table", IntStream.range(0, Math.min(8, curveData.axis().size()))
                .mapToObj(index -> orderedMap(
                    "time", curveData.axis().get(index),
                    "actualPowerMw", curveData.actual().get(index),
                    "forecastPowerMw", curveData.forecast().get(index),
                    "baselinePowerMw", curveData.baseline().get(index),
                    "deviationRate", round(((curveData.actual().get(index) - curveData.forecast().get(index)) / Math.max(curveData.forecast().get(index), 0.1)) * 100, 1),
                    "irradiance", curveData.irradiance().get(index),
                    "temperature", curveData.temperature().get(index),
                    "cloudiness", curveData.cloudiness().get(index)
                ))
                .toList()
        );
    }

    public Map<String, Object> getDispatch(String resourceUnitId) {
        ResourceUnitRecord unit = resolveResourceUnit(resourceUnitId);
        boolean faultMode = Objects.equals(unit.status(), "fault");

        return orderedMap(
            "summary", List.of(
                metric("issued", "当日下发指令", faultMode ? 9 : 14, "条", "按资源单元口径统计", "el-icon-s-promotion", "blue"),
                metric("success", "成功执行", faultMode ? 6 : 13, "条", "成功回执数", "el-icon-success", "emerald"),
                metric("rate", "执行成功率", faultMode ? 66.7 : 92.9, "%", "按资源单元整体执行率计算", "el-icon-finished", "teal"),
                metric("response", "平均响应时长", faultMode ? 182 : 96, "s", "按指令闭环时间计算", "el-icon-time", "orange")
            ),
            "executionTrend", orderedMap(
                "axis", List.of("09:00", "10:00", "11:00", "12:00", "13:00", "14:00"),
                "issued", faultMode ? List.of(1, 1, 2, 1, 2, 2) : List.of(2, 2, 3, 2, 2, 3),
                "success", faultMode ? List.of(1, 1, 1, 1, 1, 1) : List.of(2, 2, 2, 2, 2, 3),
                "responseSeconds", faultMode ? List.of(168, 172, 184, 178, 196, 182) : List.of(92, 88, 96, 102, 94, 96)
            ),
            "riskHints", List.of(
                orderedMap(
                    "title", unit.name() + " 当前天气结论",
                    "description", unit.weather().conclusion(),
                    "level", faultMode ? "高风险" : "低风险",
                    "tagType", faultMode ? "danger" : "success"
                ),
                orderedMap(
                    "title", "成员电站距离约束",
                    "description", "当前站群最远距离 " + unit.clusterRadiusKm() + "km，满足同区位共享天气建模；超出该范围建议拆分资源单元。",
                    "level", "提示",
                    "tagType", "info"
                )
            ),
            "records", List.of(
                orderedMap(
                    "issuedAt", "2026-03-23 09:15:00",
                    "commandType", "上调",
                    "targetPowerMw", round(unit.realtimePowerMw() + 0.24, 2),
                    "actualPowerMw", round(unit.realtimePowerMw() + (faultMode ? 0.08 : 0.20), 2),
                    "responseSeconds", faultMode ? 168 : 92,
                    "status", faultMode ? "处理中" : "已完成",
                    "deviationReason", faultMode ? "故障站拉低执行完整率" : "执行正常"
                ),
                orderedMap(
                    "issuedAt", "2026-03-23 12:00:00",
                    "commandType", "下调",
                    "targetPowerMw", round(unit.realtimePowerMw() - 0.36, 2),
                    "actualPowerMw", round(unit.realtimePowerMw() - (faultMode ? 0.18 : 0.34), 2),
                    "responseSeconds", faultMode ? 196 : 104,
                    "status", faultMode ? "偏差超阈" : "已完成",
                    "deviationReason", faultMode ? "天气和故障叠加导致响应不足" : "执行在安全区间"
                )
            )
        );
    }

    public Map<String, Object> getWeather(String resourceUnitId) {
        ResourceUnitRecord unit = resolveResourceUnit(resourceUnitId);
        return orderedMap(
            "summary", List.of(
                metric("weather", "当前天气", unit.weather().weather(), "", "与资源单元成员站共享", "el-icon-cloudy", "blue"),
                metric("temp", "当前温度", unit.weather().temperature(), "°C", "同城实时气象观测", "el-icon-sunny", "orange"),
                metric("irradiance", "当前辐照度", unit.weather().irradiance(), "W/m²", "用于光伏出力研判", "el-icon-s-opportunity", "teal"),
                metric("wind", "风速", unit.weather().windSpeed(), "m/s", "当前风场条件", "el-icon-time", "emerald")
            ),
            "trend", orderedMap(
                "axis", List.of("今 12:00", "今 15:00", "今 18:00", "今 21:00", "明 09:00", "明 12:00", "明 15:00", "明 18:00"),
                "series", List.of(
                    orderedMap("name", "辐照度", "data", List.of(unit.weather().irradiance() - 40, unit.weather().irradiance(), 260, 20, 540, 720, 688, 214), "type", "bar", "yAxisIndex", 1, "color", "rgba(130, 208, 255, 0.35)"),
                    orderedMap("name", "温度", "data", List.of(unit.weather().temperature(), unit.weather().temperature() + 1, unit.weather().temperature() - 2, unit.weather().temperature() - 5, unit.weather().temperature() - 2, unit.weather().temperature() + 1, unit.weather().temperature(), unit.weather().temperature() - 3), "color", "#00b578"),
                    orderedMap("name", "风速", "data", List.of(unit.weather().windSpeed(), round(unit.weather().windSpeed() + 0.2, 1), round(unit.weather().windSpeed() + 0.4, 1), round(unit.weather().windSpeed() + 0.1, 1), round(unit.weather().windSpeed() + 0.3, 1), round(unit.weather().windSpeed() + 0.5, 1), round(unit.weather().windSpeed() + 0.2, 1), unit.weather().windSpeed()), "color", "#f59b23")
                )
            ),
            "impactTable", List.of(
                orderedMap(
                    "timeRange", "今天 12:00-15:00",
                    "weather", unit.weather().weather(),
                    "irradianceRange", (unit.weather().irradiance() - 40) + "-" + unit.weather().irradiance() + " W/m²",
                    "temperatureRange", unit.weather().temperature() + "-" + (unit.weather().temperature() + 1) + "°C",
                    "windSpeedRange", unit.weather().windSpeed() + "-" + round(unit.weather().windSpeed() + 0.2, 1) + " m/s",
                    "outputLevel", "高",
                    "suggestion", "维持当前调度基线，保留上调能力"
                ),
                orderedMap(
                    "timeRange", "今天 15:00-18:00",
                    "weather", unit.weather().cloudiness(),
                    "irradianceRange", "220-420 W/m²",
                    "temperatureRange", (unit.weather().temperature() - 2) + "-" + unit.weather().temperature() + "°C",
                    "windSpeedRange", unit.weather().windSpeed() + "-" + round(unit.weather().windSpeed() + 0.4, 1) + " m/s",
                    "outputLevel", Objects.equals(unit.status(), "fault") ? "低" : "中",
                    "suggestion", Objects.equals(unit.status(), "fault")
                        ? "建议压缩上调预期并关注故障恢复"
                        : "注意云量抬升带来的短时波动"
                )
            )
        );
    }

    private ResourceUnitRecord resolveResourceUnit(String resourceUnitId) {
        return RESOURCE_UNITS.stream()
            .filter(item -> Objects.equals(item.id(), resourceUnitId))
            .findFirst()
            .orElse(RESOURCE_UNITS.getFirst());
    }

    private List<Map<String, Object>> buildRegionOptions() {
        List<Map<String, Object>> options = RESOURCE_UNITS.stream()
            .map(ResourceUnitRecord::region)
            .distinct()
            .map(region -> option(region, region))
            .toList();
        return java.util.stream.Stream.concat(
            java.util.stream.Stream.of(option("全部区域", "")),
            options.stream()
        ).toList();
    }

    private Map<String, Object> mapMetaUnit(ResourceUnitRecord unit) {
        StatusMeta meta = STATUS_META.get(unit.status());
        return orderedMap(
            "id", unit.id(),
            "name", unit.name(),
            "region", unit.region(),
            "city", unit.city(),
            "status", unit.status(),
            "statusLabel", meta.label(),
            "statusColor", meta.color(),
            "clusterRadiusKm", unit.clusterRadiusKm(),
            "stationCount", unit.stationCount(),
            "dispatchMode", unit.dispatchMode(),
            "strategyLabel", unit.strategyLabel(),
            "dispatchableCapacityMw", unit.dispatchableCapacityMw()
        );
    }

    private Map<String, Object> mapWeather(WeatherProfile weatherProfile) {
        return orderedMap(
            "weather", weatherProfile.weather(),
            "cloudiness", weatherProfile.cloudiness(),
            "temperature", weatherProfile.temperature(),
            "irradiance", weatherProfile.irradiance(),
            "humidity", weatherProfile.humidity(),
            "windSpeed", weatherProfile.windSpeed(),
            "conclusion", weatherProfile.conclusion()
        );
    }

    private Map<String, Object> mapAlarm(AlarmBrief alarmBrief) {
        return orderedMap(
            "total", alarmBrief.total(),
            "critical", alarmBrief.critical(),
            "major", alarmBrief.major(),
            "minor", alarmBrief.minor(),
            "latestTitle", alarmBrief.latestTitle(),
            "latestTime", alarmBrief.latestTime()
        );
    }

    private List<Map<String, Object>> buildMemberStations(ResourceUnitRecord unit) {
        return unit.stations().stream().map(item -> {
            double factor = STATION_STATUS_FACTOR.getOrDefault(item.status(), 1.0);
            return orderedMap(
                "id", item.id(),
                "name", item.name(),
                "capacityMw", item.capacityMw(),
                "status", item.status(),
                "statusLabel", STATUS_META.get(item.status()).label(),
                "realtimePowerMw", round(unit.realtimePowerMw() * item.weight() * factor, 2),
                "onlineRate", item.onlineRate(),
                "alarmCount", item.alarmCount(),
                "weight", item.weight(),
                "weatherText", unit.weather().weather() + " / " + unit.weather().temperature() + "°C"
            );
        }).toList();
    }

    private CurveData buildCurve(ResourceUnitRecord unit, String granularity) {
        List<String> axis = buildTimeAxis(granularity);
        double unitStatusFactor = STATION_STATUS_FACTOR.getOrDefault(unit.status(), 1.0);
        double cloudPenalty = switch (unit.weather().cloudiness()) {
            case "厚云" -> 0.55;
            case "中云" -> 0.82;
            default -> 0.94;
        };

        List<Double> actual = new java.util.ArrayList<>();
        List<Double> forecast = new java.util.ArrayList<>();
        List<Double> baseline = new java.util.ArrayList<>();
        List<Integer> irradiance = new java.util.ArrayList<>();
        List<Double> temperature = new java.util.ArrayList<>();
        List<String> cloudiness = new java.util.ArrayList<>();

        for (int index = 0; index < axis.size(); index += 1) {
            double hourStep = switch (granularity) {
                case "60m" -> 1.0;
                case "30m" -> 0.5;
                default -> 0.25;
            };
            double decimalHour = index * hourStep;
            double solarWindow = Math.max(0, Math.sin(((decimalHour - 6) / 12) * Math.PI));
            int irradianceValue = (int) round(unit.weather().irradiance() * solarWindow + Math.sin(index / 3.0) * 28, 0);
            double forecastValue = round(unit.dispatchableCapacityMw() * solarWindow * 0.96 * cloudPenalty + 0.14, 2);
            double actualValue = round(forecastValue * unitStatusFactor + Math.cos(index / 4.0) * 0.08, 2);
            double baselineValue = round(forecastValue * 0.98 + 0.1, 2);

            actual.add(Math.max(0, actualValue));
            forecast.add(Math.max(0, forecastValue));
            baseline.add(Math.max(0, baselineValue));
            irradiance.add(Math.max(0, irradianceValue));
            temperature.add(round(unit.weather().temperature() + Math.sin(index / 6.0) * 2.4, 1));
            cloudiness.add(unit.weather().cloudiness());
        }

        return new CurveData(axis, actual, forecast, baseline, irradiance, temperature, cloudiness);
    }

    private List<String> buildTimeAxis(String granularity) {
        int stepMinutes = switch (granularity) {
            case "60m" -> 60;
            case "30m" -> 30;
            default -> 15;
        };

        return IntStream.range(0, (24 * 60) / stepMinutes)
            .mapToObj(index -> {
                int totalMinutes = index * stepMinutes;
                int hour = totalMinutes / 60;
                int minute = totalMinutes % 60;
                return String.format("%02d:%02d", hour, minute);
            })
            .toList();
    }

    private Map<String, Object> metric(
        String key,
        Object title,
        Object value,
        String unit,
        String helper,
        String icon,
        String accent
    ) {
        return orderedMap(
            "key", key,
            "title", title,
            "value", value,
            "unit", unit,
            "helper", helper,
            "icon", icon,
            "accent", accent
        );
    }

    private Map<String, Object> option(String label, String value) {
        return orderedMap(
            "label", label,
            "value", value
        );
    }

    private Map<String, Object> overviewRow(
        String time,
        double realtimePowerMw,
        double dispatchableCapacityMw,
        double onlineRate,
        double forecastDeviationRate,
        String weatherText
    ) {
        return orderedMap(
            "time", time,
            "realtimePowerMw", realtimePowerMw,
            "dispatchableCapacityMw", dispatchableCapacityMw,
            "onlineRate", onlineRate,
            "forecastDeviationRate", forecastDeviationRate,
            "weatherText", weatherText
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

    private record WeatherProfile(
        String weather,
        String cloudiness,
        int temperature,
        int irradiance,
        int humidity,
        double windSpeed,
        String conclusion
    ) {
    }

    private record AlarmBrief(
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
        double capacityMw,
        double weight,
        String status,
        double onlineRate,
        int alarmCount
    ) {
    }

    private record ResourceUnitRecord(
        String id,
        String name,
        String region,
        String city,
        String status,
        int clusterRadiusKm,
        int stationCount,
        double dispatchableCapacityMw,
        double realtimePowerMw,
        double todayEnergyMwh,
        double upRegulationMw,
        double downRegulationMw,
        double onlineRate,
        double forecastAccuracy,
        double forecastDeviationRate,
        String dispatchMode,
        String strategyLabel,
        WeatherProfile weather,
        AlarmBrief alarm,
        List<MemberStationRecord> stations
    ) {
    }

    private record CurveData(
        List<String> axis,
        List<Double> actual,
        List<Double> forecast,
        List<Double> baseline,
        List<Integer> irradiance,
        List<Double> temperature,
        List<String> cloudiness
    ) {
    }
}
