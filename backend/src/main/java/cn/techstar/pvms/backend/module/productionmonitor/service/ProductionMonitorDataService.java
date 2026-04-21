package cn.techstar.pvms.backend.module.productionmonitor.service;

import cn.techstar.pvms.backend.module.productionmonitor.repository.ProductionMonitorCurveMapper;
import cn.techstar.pvms.backend.module.productionmonitor.repository.ProductionMonitorDispatchRecordMapper;
import cn.techstar.pvms.backend.module.productionmonitor.repository.ProductionMonitorResourceUnitMapper;
import cn.techstar.pvms.backend.module.productionmonitor.repository.ProductionMonitorStationSnapshotMapper;
import cn.techstar.pvms.backend.module.productionmonitor.repository.ProductionMonitorWeatherSnapshotMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProductionMonitorDataService {

    private static final LocalDate DEFAULT_BIZ_DATE = LocalDate.of(2026, 3, 30);
    private static final String CURRENT_TIME_LABEL = "14:00";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Map<String, StatusMeta> STATUS_META = buildStatusMeta();
    private static final Map<String, Double> STATUS_DISPATCH_FACTOR = Map.of(
        "normal", 1.00,
        "warning", 0.92,
        "maintenance", 0.78,
        "fault", 0.58,
        "offline", 0.22
    );

    private final ProductionMonitorResourceUnitMapper resourceUnitMapper;
    private final ProductionMonitorStationSnapshotMapper stationSnapshotMapper;
    private final ProductionMonitorWeatherSnapshotMapper weatherSnapshotMapper;
    private final ProductionMonitorCurveMapper curveMapper;
    private final ProductionMonitorDispatchRecordMapper dispatchRecordMapper;
    private final ProductionMonitorSeriesAggregator seriesAggregator;

    public ProductionMonitorDataService(
        ProductionMonitorResourceUnitMapper resourceUnitMapper,
        ProductionMonitorStationSnapshotMapper stationSnapshotMapper,
        ProductionMonitorWeatherSnapshotMapper weatherSnapshotMapper,
        ProductionMonitorCurveMapper curveMapper,
        ProductionMonitorDispatchRecordMapper dispatchRecordMapper,
        ProductionMonitorSeriesAggregator seriesAggregator
    ) {
        this.resourceUnitMapper = resourceUnitMapper;
        this.stationSnapshotMapper = stationSnapshotMapper;
        this.weatherSnapshotMapper = weatherSnapshotMapper;
        this.curveMapper = curveMapper;
        this.dispatchRecordMapper = dispatchRecordMapper;
        this.seriesAggregator = seriesAggregator;
    }

    public Map<String, Object> getMeta() {
        List<ProductionMonitorResourceUnitMapper.ResourceUnitRow> resourceUnits = resourceUnitMapper.findAll();
        Map<String, List<ProductionMonitorStationSnapshotMapper.StationSnapshotRow>> stationsByUnit = groupStationsByUnit();

        return orderedMap(
            "defaultResourceUnitId", resourceUnits.getFirst().id(),
            "regionOptions", buildRegionOptions(resourceUnits),
            "resourceUnits", resourceUnits.stream()
                .map(unit -> mapMetaUnit(unit, stationsByUnit.getOrDefault(unit.id(), List.of())))
                .toList(),
            "stations", stationSnapshotMapper.findAll().stream()
                .map(this::mapMetaStation)
                .toList()
        );
    }

    public Map<String, Object> getOverview(String resourceUnitId) {
        Context context = resolveContext(resourceUnitId);
        List<ProductionMonitorSeriesAggregator.AggregatedPoint> points =
            seriesAggregator.aggregate(context.curveRows(), "15m");

        double dispatchableCapacityMw = calculateDispatchableCapacityMw(context.stations());
        double realtimePowerMw = calculateRealtimePowerMw(context.stations());
        double todayEnergyMwh = calculateTodayEnergyMwh(points);
        double upRegulationMw = calculateUpRegulationMw(context.stations(), realtimePowerMw);
        double downRegulationMw = calculateDownRegulationMw(realtimePowerMw);
        double onlineRate = calculateOnlineRate(context.stations());
        double accuracy = calculateForecastAccuracy(points);

        return orderedMap(
            "info", mapMetaUnit(context.unit(), context.stations()),
            "kpis", List.of(
                metric("realtime", "实时总出力", realtimePowerMw, "MW", "当前资源单元聚合总出力", "el-icon-lightning", "blue"),
                metric("today-energy", "当日累计电量", todayEnergyMwh, "MWh", "按 15 分钟时序累计折算", "el-icon-data-analysis", "emerald"),
                metric("up", "上调可调能力", upRegulationMw, "MW", "由装机剩余空间和当前状态推导", "el-icon-top", "teal"),
                metric("down", "下调可调能力", downRegulationMw, "MW", "按当前实时出力可回撤空间折算", "el-icon-bottom", "orange"),
                metric("online", "在线率", onlineRate, "%", "按成员电站在线质量聚合", "el-icon-success", "teal"),
                metric("accuracy", "预测准确率", accuracy, "%", "由预测曲线与实际曲线偏差反推", "el-icon-s-marketing", "blue")
            ),
            "memberStations", buildMemberStations(context.stations(), context.weather()),
            "weatherBrief", mapWeather(context.weather()),
            "alarmBrief", mapAlarm(context.unit()),
            "summaryTable", buildOverviewSummaryTable(points, dispatchableCapacityMw, onlineRate, context.weather().conclusion())
        );
    }

    public Map<String, Object> getOutput(String resourceUnitId, String granularity) {
        Context context = resolveContext(resourceUnitId);
        String resolvedGranularity = normalizeGranularity(granularity);
        List<ProductionMonitorSeriesAggregator.AggregatedPoint> points =
            seriesAggregator.aggregate(context.curveRows(), resolvedGranularity);
        double realtimePowerMw = calculateRealtimePowerMw(context.stations());
        double upRegulationMw = calculateUpRegulationMw(context.stations(), realtimePowerMw);
        double downRegulationMw = calculateDownRegulationMw(realtimePowerMw);

        return orderedMap(
            "summary", List.of(
                metric("peak", "峰值出力", round(points.stream().mapToDouble(ProductionMonitorSeriesAggregator.AggregatedPoint::pvPowerKw).max().orElse(0) / 1000.0, 2), "MW", "按当前粒度计算的峰值", "el-icon-data-line", "blue"),
                metric("deviation", "预测偏差率", calculateDeviationRate(points, CURRENT_TIME_LABEL), "%", "基于当前时段的预测偏差", "el-icon-warning-outline", "orange"),
                metric("irradiance", "峰值辐照度", points.stream().mapToInt(ProductionMonitorSeriesAggregator.AggregatedPoint::irradiance).max().orElse(0), "W/m²", "同区域共享天气趋势", "el-icon-s-opportunity", "teal"),
                metric("granularity", "当前粒度", resolvedGranularity.replace("m", ""), "min", "切换粒度后曲线和表格联动", "el-icon-time", "emerald")
            ),
            "curve", orderedMap(
                "axis", seriesAggregator.axis(points),
                "actual", seriesAggregator.toMwSeries(points, ProductionMonitorSeriesAggregator.AggregatedPoint::pvPowerKw),
                "forecast", seriesAggregator.toMwSeries(points, ProductionMonitorSeriesAggregator.AggregatedPoint::forecastPowerKw),
                "baseline", seriesAggregator.toMwSeries(points, ProductionMonitorSeriesAggregator.AggregatedPoint::baselinePowerKw),
                "irradiance", seriesAggregator.toIntegerSeries(points, ProductionMonitorSeriesAggregator.AggregatedPoint::irradiance),
                "powerUnit", "MW",
                "weatherUnit", "W/m²"
            ),
            "weatherTrend", orderedMap(
                "axis", seriesAggregator.axis(points),
                "series", List.of(
                    orderedMap("name", "辐照度", "data", seriesAggregator.toIntegerSeries(points, ProductionMonitorSeriesAggregator.AggregatedPoint::irradiance), "type", "bar", "yAxisIndex", 1, "color", "rgba(130, 208, 255, 0.35)"),
                    orderedMap("name", "温度", "data", seriesAggregator.toKwSeries(points, ProductionMonitorSeriesAggregator.AggregatedPoint::temperature), "color", "#00b578")
                )
            ),
            "contributionRanking", buildContributionRanking(context.stations(), context.weather()),
            "table", buildOutputTable(points, context.weather().cloudiness(), upRegulationMw, downRegulationMw)
        );
    }

    public Map<String, Object> getLoad(String resourceUnitId, LocalDate bizDate, String granularity) {
        LocalDate resolvedBizDate = resolveBizDate(bizDate);
        String resolvedGranularity = normalizeGranularity(granularity);
        List<ProductionMonitorStationSnapshotMapper.StationSnapshotRow> stations = stationSnapshotMapper.findAll().stream()
            .filter(ProductionMonitorStationSnapshotMapper.StationSnapshotRow::loadVisible)
            .sorted(java.util.Comparator.comparingInt(ProductionMonitorStationSnapshotMapper.StationSnapshotRow::sortIndex))
            .toList();
        Map<String, List<ProductionMonitorCurveMapper.CurveRow>> curvesByStation = curveMapper.findLoadVisibleByDate(resolvedBizDate).stream()
            .collect(Collectors.groupingBy(ProductionMonitorCurveMapper.CurveRow::stationId, LinkedHashMap::new, Collectors.toList()));

        List<Map<String, Object>> stationRows = stations.stream()
            .map(station -> buildLoadStationRow(station, curvesByStation.getOrDefault(station.id(), List.of()), resolvedGranularity))
            .toList();

        double totalLoadKw = stations.stream().mapToDouble(ProductionMonitorStationSnapshotMapper.StationSnapshotRow::loadKw).sum();
        double totalPvKw = stations.stream().mapToDouble(ProductionMonitorStationSnapshotMapper.StationSnapshotRow::realtimePowerKw).sum();
        double totalAdjustableKw = stations.stream()
            .mapToDouble(item -> Math.max(item.loadKw() - item.realtimePowerKw(), 0))
            .sum();
        double averageRampRate = stationRows.stream()
            .mapToDouble(item -> ((Number) item.get("maxRampRate")).doubleValue())
            .average()
            .orElse(0);
        long onlineCount = stations.stream().filter(item -> !"offline".equals(item.status())).count();

        return orderedMap(
            "summary", orderedMap(
                "totalLoadMw", round(totalLoadKw / 1000.0, 1),
                "totalPvOutputMw", round(totalPvKw / 1000.0, 1),
                "totalAdjustableMw", round(totalAdjustableKw / 1000.0, 1),
                "avgRampRate", round(averageRampRate, 1),
                "stationCount", stations.size(),
                "onlineCount", onlineCount
            ),
            "stations", stationRows
        );
    }

    public Map<String, Object> getGridInteraction(String stationId) {
        // Resolve station snapshot (fall back to first if not found)
        ProductionMonitorStationSnapshotMapper.StationSnapshotRow station = stationSnapshotMapper.findAll().stream()
            .filter(s -> Objects.equals(s.id(), stationId))
            .findFirst()
            .orElseGet(() -> stationSnapshotMapper.findAll().getFirst());

        // Try to get real curve data for this station
        List<ProductionMonitorCurveMapper.CurveRow> curveRows = curveMapper.findLoadVisibleByDate(DEFAULT_BIZ_DATE).stream()
            .filter(row -> Objects.equals(row.stationId(), station.id()))
            .toList();

        List<ProductionMonitorSeriesAggregator.AggregatedPoint> points = seriesAggregator.aggregate(curveRows, "15m");

        // No real data → generate a realistic synthetic day curve
        if (points.isEmpty()) {
            points = buildSyntheticPoints(station);
        }

        return orderedMap(
            "stationId", station.id(),
            "stationName", station.name(),
            "times", seriesAggregator.axis(points),
            "series", List.of(
                orderedMap("name", "负荷", "data",
                    seriesAggregator.toKwSeries(points, ProductionMonitorSeriesAggregator.AggregatedPoint::loadKw)),
                orderedMap("name", "光伏出力", "data",
                    seriesAggregator.toKwSeries(points, ProductionMonitorSeriesAggregator.AggregatedPoint::pvPowerKw)),
                orderedMap("name", "电网交互(负荷-出力)", "data",
                    seriesAggregator.toKwSeries(points, p -> p.loadKw() - p.pvPowerKw()), "area", true)
            )
        );
    }

    private List<ProductionMonitorSeriesAggregator.AggregatedPoint> buildSyntheticPoints(
        ProductionMonitorStationSnapshotMapper.StationSnapshotRow station
    ) {
        // Use snapshot values as scale anchors; fall back to capacity-derived estimates
        double peakPvKw  = Math.max(station.realtimePowerKw(), station.capacityMw() * 700.0);
        double peakLoadKw = Math.max(station.loadKw(), station.capacityMw() * 850.0);

        List<ProductionMonitorSeriesAggregator.AggregatedPoint> points = new ArrayList<>(96);
        for (int slot = 0; slot < 96; slot++) {
            double h = slot / 4.0;  // fractional hour

            // PV: bell curve 06:00 → 18:00, peaks at noon
            double pvFactor = (h >= 6.0 && h <= 18.0)
                ? Math.sin((h - 6.0) / 12.0 * Math.PI) : 0.0;
            double pvKw = round(peakPvKw * pvFactor, 1);

            // Load: gentle sine waves giving morning and evening humps, floor at 35%
            double loadFactor = 0.65
                + 0.15 * Math.sin((h - 3.0) / 12.0 * Math.PI)
                + 0.10 * Math.sin((h - 16.0) /  6.0 * Math.PI);
            double loadKw = round(peakLoadKw * Math.max(0.35, loadFactor), 1);

            int totalMinutes = slot * 15;
            String label = "%02d:%02d".formatted(totalMinutes / 60, totalMinutes % 60);

            points.add(new ProductionMonitorSeriesAggregator.AggregatedPoint(
                label, loadKw, pvKw,
                round(pvKw * 0.94, 1),   // forecast ≈ 94% of actual
                round(pvKw * 0.97, 1),   // baseline ≈ 97% of actual
                (int) (650.0 * pvFactor), // irradiance (W/m²)
                25.0 + pvFactor * 8.0,   // temperature rises with sun
                15                        // 15-min step
            ));
        }
        return points;
    }

    public Map<String, Object> getDispatch(String resourceUnitId) {
        Context context = resolveContext(resourceUnitId);
        List<ProductionMonitorDispatchRecordMapper.DispatchRecordRow> records = dispatchRecordMapper.findByResourceUnitId(context.unit().id());
        Map<String, List<ProductionMonitorDispatchRecordMapper.DispatchRecordRow>> recordsByHour = records.stream()
            .collect(Collectors.groupingBy(item -> formatHour(item.issuedAt()), LinkedHashMap::new, Collectors.toList()));
        List<String> axis = List.of("09:00", "10:00", "11:00", "12:00", "13:00", "14:00");
        long successCount = records.stream().filter(item -> Objects.equals(item.status(), "已完成")).count();
        double averageResponseSeconds = records.stream().mapToInt(ProductionMonitorDispatchRecordMapper.DispatchRecordRow::responseSeconds).average().orElse(0);
        double upRegulationMw = calculateUpRegulationMw(context.stations(), calculateRealtimePowerMw(context.stations()));
        double adjustmentUsedKw = records.stream()
            .mapToDouble(item -> Math.abs(item.targetPowerMw() - item.actualPowerMw()) * 1000.0)
            .sum();

        return orderedMap(
            "summary", List.of(
                metric("issued", "当日下发指令", records.size(), "条", "按资源单元口径统计", "el-icon-s-promotion", "blue"),
                metric("success", "成功执行", successCount, "条", "成功回执数", "el-icon-success", "emerald"),
                metric("rate", "执行成功率", records.isEmpty() ? 0 : round(successCount * 100.0 / records.size(), 1), "%", "按资源单元整体执行率计算", "el-icon-finished", "teal"),
                metric("response", "平均响应时长", round(averageResponseSeconds, 0), "s", "按指令闭环时间计算", "el-icon-time", "orange"),
                metric("adj-up", "今日可调上限", round(upRegulationMw * 1000.0, 0), "kW", "储能可放电 + 光伏可上调", "el-icon-top", "emerald"),
                metric("adj-used", "今日已调用量", round(adjustmentUsedKw, 0), "kW", "累计调度消耗可调资源", "el-icon-data-board", "blue")
            ),
            "executionTrend", orderedMap(
                "axis", axis,
                "issued", axis.stream().map(hour -> recordsByHour.getOrDefault(hour, List.of()).size()).toList(),
                "success", axis.stream().map(hour -> (int) recordsByHour.getOrDefault(hour, List.of()).stream().filter(item -> Objects.equals(item.status(), "已完成")).count()).toList(),
                "responseSeconds", axis.stream().map(hour -> (int) Math.round(recordsByHour.getOrDefault(hour, List.of()).stream().mapToInt(ProductionMonitorDispatchRecordMapper.DispatchRecordRow::responseSeconds).average().orElse(0))).toList()
            ),
            "riskHints", buildRiskHints(context.unit(), context.weather()),
            "records", records.stream().map(this::mapDispatchRecord).toList()
        );
    }

    public Map<String, Object> getWeather(String resourceUnitId) {
        Context context = resolveContext(resourceUnitId);
        ProductionMonitorWeatherSnapshotMapper.WeatherSnapshotRow weather = context.weather();

        return orderedMap(
            "summary", List.of(
                metric("weather", "当前天气", weather.weather(), "", "与资源单元成员站共享", "el-icon-cloudy", "blue"),
                metric("temp", "当前温度", weather.temperature(), "°C", "同城实时气象观测", "el-icon-sunny", "orange"),
                metric("irradiance", "当前辐照度", weather.irradiance(), "W/m²", "用于光伏出力研判", "el-icon-s-opportunity", "teal"),
                metric("wind", "风速", weather.windSpeed(), "m/s", "当前风场条件", "el-icon-time", "emerald")
            ),
            "trend", buildWeatherTrend(weather),
            "impactTable", buildWeatherImpactTable(context.unit(), weather)
        );
    }

    private Context resolveContext(String resourceUnitId) {
        List<ProductionMonitorResourceUnitMapper.ResourceUnitRow> units = resourceUnitMapper.findAll();
        ProductionMonitorResourceUnitMapper.ResourceUnitRow unit = units.stream()
            .filter(item -> Objects.equals(item.id(), resourceUnitId))
            .findFirst()
            .orElse(units.getFirst());

        List<ProductionMonitorStationSnapshotMapper.StationSnapshotRow> stations = stationSnapshotMapper.findAll().stream()
            .filter(item -> Objects.equals(item.resourceUnitId(), unit.id()))
            .sorted(java.util.Comparator.comparingInt(ProductionMonitorStationSnapshotMapper.StationSnapshotRow::sortIndex))
            .toList();
        ProductionMonitorWeatherSnapshotMapper.WeatherSnapshotRow weather = weatherSnapshotMapper.findAll().stream()
            .filter(item -> Objects.equals(item.resourceUnitId(), unit.id()))
            .findFirst()
            .orElseThrow();
        List<ProductionMonitorCurveMapper.CurveRow> curveRows =
            curveMapper.findByResourceUnitIdAndDate(unit.id(), DEFAULT_BIZ_DATE);

        return new Context(unit, stations, weather, curveRows);
    }

    private Map<String, List<ProductionMonitorStationSnapshotMapper.StationSnapshotRow>> groupStationsByUnit() {
        return stationSnapshotMapper.findAll().stream()
            .collect(Collectors.groupingBy(
                ProductionMonitorStationSnapshotMapper.StationSnapshotRow::resourceUnitId,
                LinkedHashMap::new,
                Collectors.toList()
            ));
    }

    private List<Map<String, Object>> buildRegionOptions(List<ProductionMonitorResourceUnitMapper.ResourceUnitRow> resourceUnits) {
        LinkedHashSet<String> regions = new LinkedHashSet<>();
        resourceUnits.stream().map(ProductionMonitorResourceUnitMapper.ResourceUnitRow::region).forEach(regions::add);
        List<Map<String, Object>> options = new ArrayList<>();
        options.add(option("全部区域", ""));
        regions.forEach(region -> options.add(option(region, region)));
        return options;
    }

    private Map<String, Object> mapMetaUnit(
        ProductionMonitorResourceUnitMapper.ResourceUnitRow unit,
        List<ProductionMonitorStationSnapshotMapper.StationSnapshotRow> stations
    ) {
        StatusMeta meta = STATUS_META.getOrDefault(unit.status(), new StatusMeta(unit.status(), "#909399"));
        return orderedMap(
            "id", unit.id(),
            "name", unit.name(),
            "region", unit.region(),
            "city", unit.city(),
            "status", unit.status(),
            "statusLabel", meta.label(),
            "statusColor", meta.color(),
            "clusterRadiusKm", unit.clusterRadiusKm(),
            "stationCount", stations.size(),
            "stationIds", stations.stream().map(ProductionMonitorStationSnapshotMapper.StationSnapshotRow::id).toList(),
            "dispatchMode", unit.dispatchMode(),
            "strategyLabel", unit.strategyLabel(),
            "dispatchableCapacityMw", calculateDispatchableCapacityMw(stations)
        );
    }

    private Map<String, Object> mapMetaStation(ProductionMonitorStationSnapshotMapper.StationSnapshotRow station) {
        return orderedMap(
            "id", station.id(),
            "name", station.name(),
            "resourceUnitId", station.resourceUnitId(),
            "capacityMw", round(station.capacityMw(), 1),
            "status", station.status(),
            "statusLabel", resolveStatusLabel(station.status()),
            "onlineRate", round(station.onlineRate(), 1),
            "alarmCount", station.alarmCount(),
            "sortIndex", station.sortIndex()
        );
    }

    private List<Map<String, Object>> buildMemberStations(
        List<ProductionMonitorStationSnapshotMapper.StationSnapshotRow> stations,
        ProductionMonitorWeatherSnapshotMapper.WeatherSnapshotRow weather
    ) {
        return stations.stream().map(station -> orderedMap(
            "id", station.id(),
            "name", station.name(),
            "capacityMw", round(station.capacityMw(), 1),
            "status", station.status(),
            "statusLabel", resolveStatusLabel(station.status()),
            "realtimePowerMw", round(station.realtimePowerKw() / 1000.0, 2),
            "onlineRate", round(station.onlineRate(), 1),
            "adjustableCapacityMw", round(Math.max(station.loadKw() - station.realtimePowerKw(), 0) / 1000.0, 2),
            "alarmCount", station.alarmCount(),
            "weight", station.weight(),
            "weatherText", weather.weather() + " / " + weather.temperature() + "°C"
        )).toList();
    }

    private List<Map<String, Object>> buildOverviewSummaryTable(
        List<ProductionMonitorSeriesAggregator.AggregatedPoint> points,
        double dispatchableCapacityMw,
        double onlineRate,
        String weatherConclusion
    ) {
        return List.of("09:00", "12:00", "15:00").stream()
            .map(label -> {
                ProductionMonitorSeriesAggregator.AggregatedPoint point = resolvePoint(points, label);
                return orderedMap(
                    "time", label,
                    "realtimePowerMw", round(point.pvPowerKw() / 1000.0, 2),
                    "dispatchableCapacityMw", dispatchableCapacityMw,
                    "onlineRate", round(onlineRate, 1),
                    "forecastDeviationRate", calculateDeviationRate(List.of(point), label),
                    "weatherText", weatherConclusion
                );
            })
            .toList();
    }

    private List<Map<String, Object>> buildContributionRanking(
        List<ProductionMonitorStationSnapshotMapper.StationSnapshotRow> stations,
        ProductionMonitorWeatherSnapshotMapper.WeatherSnapshotRow weather
    ) {
        return stations.stream()
            .sorted((left, right) -> Double.compare(right.realtimePowerKw(), left.realtimePowerKw()))
            .map(station -> orderedMap(
                "id", station.id(),
                "name", station.name(),
                "realtimePowerMw", round(station.realtimePowerKw() / 1000.0, 2),
                "shareRate", round(station.weight() * 100.0, 0),
                "weatherText", weather.weather() + " / " + weather.temperature() + "°C"
            ))
            .toList();
    }

    private List<Map<String, Object>> buildOutputTable(
        List<ProductionMonitorSeriesAggregator.AggregatedPoint> points,
        String cloudiness,
        double upRegulationMw,
        double downRegulationMw
    ) {
        List<Map<String, Object>> table = new ArrayList<>();
        for (int index = 0; index < Math.min(points.size(), 8); index += 1) {
            ProductionMonitorSeriesAggregator.AggregatedPoint point = points.get(index);
            table.add(orderedMap(
                "time", point.label(),
                "actualPowerMw", round(point.pvPowerKw() / 1000.0, 2),
                "forecastPowerMw", round(point.forecastPowerKw() / 1000.0, 2),
                "baselinePowerMw", round(point.baselinePowerKw() / 1000.0, 2),
                "deviationRate", point.forecastPowerKw() == 0 ? 0 : round((point.pvPowerKw() - point.forecastPowerKw()) / point.forecastPowerKw() * 100.0, 1),
                "irradiance", point.irradiance(),
                "temperature", point.temperature(),
                "cloudiness", cloudiness,
                "maxUpCapacityKw", round(upRegulationMw * 1000.0 * (0.82 + index * 0.03), 0),
                "maxDownCapacityKw", round(downRegulationMw * 1000.0 * (0.78 + index * 0.02), 0)
            ));
        }
        return table;
    }

    private Map<String, Object> buildLoadStationRow(
        ProductionMonitorStationSnapshotMapper.StationSnapshotRow station,
        List<ProductionMonitorCurveMapper.CurveRow> curveRows,
        String granularity
    ) {
        List<ProductionMonitorSeriesAggregator.AggregatedPoint> points = seriesAggregator.aggregate(curveRows, granularity);
        return orderedMap(
            "id", station.id(),
            "name", station.name(),
            "capacityKwp", round(station.capacityMw() * 1000.0, 0),
            "realtimePowerKw", round(station.realtimePowerKw(), 1),
            "loadKw", round(station.loadKw(), 1),
            "adjustableKw", round(Math.max(station.loadKw() - station.realtimePowerKw(), 0), 1),
            "maxRampRate", seriesAggregator.calculateMaxRampRate(points),
            "status", station.status(),
            "gridInteraction", orderedMap(
                "times", seriesAggregator.axis(points),
                "series", List.of(
                    orderedMap("name", "负荷", "data", seriesAggregator.toKwSeries(points, ProductionMonitorSeriesAggregator.AggregatedPoint::loadKw)),
                    orderedMap("name", "光伏出力", "data", seriesAggregator.toKwSeries(points, ProductionMonitorSeriesAggregator.AggregatedPoint::pvPowerKw)),
                    orderedMap("name", "电网交互(负荷-出力)", "data", seriesAggregator.toKwSeries(points, point -> point.loadKw() - point.pvPowerKw()), "area", true)
                )
            )
        );
    }

    private List<Map<String, Object>> buildRiskHints(
        ProductionMonitorResourceUnitMapper.ResourceUnitRow unit,
        ProductionMonitorWeatherSnapshotMapper.WeatherSnapshotRow weather
    ) {
        boolean highRisk = Objects.equals(unit.status(), "fault") || Objects.equals(unit.status(), "offline");
        boolean mediumRisk = Objects.equals(unit.status(), "warning") || Objects.equals(unit.status(), "maintenance");
        String riskLevel = highRisk ? "高风险" : mediumRisk ? "中风险" : "低风险";
        String tagType = highRisk ? "danger" : mediumRisk ? "warning" : "success";

        return List.of(
            orderedMap(
                "title", unit.name() + " 当前天气结论",
                "description", weather.conclusion(),
                "level", riskLevel,
                "tagType", tagType
            ),
            orderedMap(
                "title", "成员电站距离约束",
                "description", "当前站群最远距离 " + unit.clusterRadiusKm() + "km，满足同区位共享天气建模；超出该范围建议拆分资源单元。",
                "level", "提示",
                "tagType", "info"
            )
        );
    }

    private Map<String, Object> buildWeatherTrend(ProductionMonitorWeatherSnapshotMapper.WeatherSnapshotRow weather) {
        return orderedMap(
            "axis", List.of("今天 12:00", "今天 15:00", "今天 18:00", "今天 21:00", "明天 09:00", "明天 12:00", "明天 15:00", "明天 18:00"),
            "series", List.of(
                orderedMap("name", "辐照度", "data", List.of(Math.max(weather.irradiance() - 40, 0), weather.irradiance(), Math.max(weather.irradiance() - 180, 120), 20, Math.max(weather.irradiance() - 120, 260), Math.min(weather.irradiance() + 32, 780), Math.max(weather.irradiance() - 24, 240), 214), "type", "bar", "yAxisIndex", 1, "color", "rgba(130, 208, 255, 0.35)"),
                orderedMap("name", "温度", "data", List.of(weather.temperature(), weather.temperature() + 1, weather.temperature() - 2, weather.temperature() - 5, weather.temperature() - 2, weather.temperature() + 1, weather.temperature(), weather.temperature() - 3), "color", "#00b578"),
                orderedMap("name", "风速", "data", List.of(weather.windSpeed(), round(weather.windSpeed() + 0.2, 1), round(weather.windSpeed() + 0.4, 1), round(weather.windSpeed() + 0.1, 1), round(weather.windSpeed() + 0.3, 1), round(weather.windSpeed() + 0.5, 1), round(weather.windSpeed() + 0.2, 1), weather.windSpeed()), "color", "#f59b23")
            )
        );
    }

    private List<Map<String, Object>> buildWeatherImpactTable(
        ProductionMonitorResourceUnitMapper.ResourceUnitRow unit,
        ProductionMonitorWeatherSnapshotMapper.WeatherSnapshotRow weather
    ) {
        boolean constrained = Objects.equals(unit.status(), "fault") || Objects.equals(unit.status(), "offline");
        return List.of(
            orderedMap(
                "timeRange", "今天 12:00-15:00",
                "weather", weather.weather(),
                "irradianceRange", Math.max(weather.irradiance() - 40, 0) + "-" + weather.irradiance() + " W/m²",
                "temperatureRange", weather.temperature() + "-" + (weather.temperature() + 1) + "°C",
                "windSpeedRange", weather.windSpeed() + "-" + round(weather.windSpeed() + 0.2, 1) + " m/s",
                "outputLevel", "高",
                "suggestion", "维持当前调度基线，保留上调能力。"
            ),
            orderedMap(
                "timeRange", "今天 15:00-18:00",
                "weather", weather.cloudiness(),
                "irradianceRange", Math.max(weather.irradiance() - 220, 120) + "-" + Math.max(weather.irradiance() - 40, 220) + " W/m²",
                "temperatureRange", (weather.temperature() - 2) + "-" + weather.temperature() + "°C",
                "windSpeedRange", weather.windSpeed() + "-" + round(weather.windSpeed() + 0.4, 1) + " m/s",
                "outputLevel", constrained ? "低" : "中",
                "suggestion", constrained ? "建议压缩上调预期并持续关注设备恢复。" : "注意云量抬升带来的短时波动。"
            )
        );
    }

    private Map<String, Object> mapWeather(ProductionMonitorWeatherSnapshotMapper.WeatherSnapshotRow weather) {
        return orderedMap(
            "weather", weather.weather(),
            "cloudiness", weather.cloudiness(),
            "temperature", weather.temperature(),
            "irradiance", weather.irradiance(),
            "humidity", weather.humidity(),
            "windSpeed", weather.windSpeed(),
            "conclusion", weather.conclusion()
        );
    }

    private Map<String, Object> mapAlarm(ProductionMonitorResourceUnitMapper.ResourceUnitRow unit) {
        return orderedMap(
            "total", unit.alarmTotal(),
            "critical", unit.alarmCritical(),
            "major", unit.alarmMajor(),
            "minor", unit.alarmMinor(),
            "latestTitle", unit.latestAlarmTitle(),
            "latestTime", formatDateTime(unit.latestAlarmTime())
        );
    }

    private Map<String, Object> mapDispatchRecord(ProductionMonitorDispatchRecordMapper.DispatchRecordRow item) {
        return orderedMap(
            "issuedAt", formatDateTime(item.issuedAt()),
            "commandType", item.commandType(),
            "targetPowerMw", round(item.targetPowerMw(), 2),
            "actualPowerMw", round(item.actualPowerMw(), 2),
            "responseSeconds", item.responseSeconds(),
            "status", item.status(),
            "deviationReason", item.deviationReason()
        );
    }

    private double calculateDispatchableCapacityMw(List<ProductionMonitorStationSnapshotMapper.StationSnapshotRow> stations) {
        double value = stations.stream()
            .mapToDouble(item -> item.capacityMw() * STATUS_DISPATCH_FACTOR.getOrDefault(item.status(), 0.85))
            .sum();
        return round(value, 2);
    }

    private double calculateRealtimePowerMw(List<ProductionMonitorStationSnapshotMapper.StationSnapshotRow> stations) {
        return round(stations.stream().mapToDouble(ProductionMonitorStationSnapshotMapper.StationSnapshotRow::realtimePowerKw).sum() / 1000.0, 2);
    }

    private double calculateTodayEnergyMwh(List<ProductionMonitorSeriesAggregator.AggregatedPoint> points) {
        return round(points.stream().mapToDouble(ProductionMonitorSeriesAggregator.AggregatedPoint::pvPowerKw).sum() * 0.25 / 1000.0, 1);
    }

    private double calculateUpRegulationMw(List<ProductionMonitorStationSnapshotMapper.StationSnapshotRow> stations, double realtimePowerMw) {
        double totalCapacityMw = stations.stream().mapToDouble(ProductionMonitorStationSnapshotMapper.StationSnapshotRow::capacityMw).sum();
        return round(Math.max(totalCapacityMw - realtimePowerMw, 0) * 0.75, 2);
    }

    private double calculateDownRegulationMw(double realtimePowerMw) {
        return round(realtimePowerMw * 0.35, 2);
    }

    private double calculateOnlineRate(List<ProductionMonitorStationSnapshotMapper.StationSnapshotRow> stations) {
        return round(stations.stream().mapToDouble(ProductionMonitorStationSnapshotMapper.StationSnapshotRow::onlineRate).average().orElse(0), 1);
    }

    private double calculateForecastAccuracy(List<ProductionMonitorSeriesAggregator.AggregatedPoint> points) {
        double averageDeviation = points.stream()
            .filter(point -> point.forecastPowerKw() > 0)
            .mapToDouble(point -> Math.abs(point.pvPowerKw() - point.forecastPowerKw()) / point.forecastPowerKw() * 100.0)
            .average()
            .orElse(0);
        return round(Math.max(0, 100.0 - averageDeviation), 1);
    }

    private double calculateDeviationRate(List<ProductionMonitorSeriesAggregator.AggregatedPoint> points, String preferredLabel) {
        ProductionMonitorSeriesAggregator.AggregatedPoint point = resolvePoint(points, preferredLabel);
        if (point.forecastPowerKw() <= 0) {
            return 0;
        }
        return round((point.pvPowerKw() - point.forecastPowerKw()) / point.forecastPowerKw() * 100.0, 1);
    }

    private ProductionMonitorSeriesAggregator.AggregatedPoint resolvePoint(
        List<ProductionMonitorSeriesAggregator.AggregatedPoint> points,
        String preferredLabel
    ) {
        return points.stream()
            .filter(point -> Objects.equals(point.label(), preferredLabel))
            .findFirst()
            .orElse(points.get(Math.min(points.size() - 1, points.size() / 2)));
    }

    private String normalizeGranularity(String granularity) {
        return switch (granularity) {
            case "30m", "60m" -> granularity;
            default -> "15m";
        };
    }

    private LocalDate resolveBizDate(LocalDate bizDate) {
        return bizDate == null ? DEFAULT_BIZ_DATE : bizDate;
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "" : DATE_TIME_FORMATTER.format(value);
    }

    private String formatHour(LocalDateTime value) {
        return value == null ? "" : value.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private String resolveStatusLabel(String status) {
        return STATUS_META.getOrDefault(status, new StatusMeta(status, "#909399")).label();
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
        return orderedMap("label", label, "value", value);
    }

    private Map<String, Object> orderedMap(Object... entries) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            result.put((String) entries[index], entries[index + 1]);
        }
        return result;
    }

    private double round(double value, int scale) {
        double factor = Math.pow(10, scale);
        return Math.round(value * factor) / factor;
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

    private record Context(
        ProductionMonitorResourceUnitMapper.ResourceUnitRow unit,
        List<ProductionMonitorStationSnapshotMapper.StationSnapshotRow> stations,
        ProductionMonitorWeatherSnapshotMapper.WeatherSnapshotRow weather,
        List<ProductionMonitorCurveMapper.CurveRow> curveRows
    ) {
    }
}
