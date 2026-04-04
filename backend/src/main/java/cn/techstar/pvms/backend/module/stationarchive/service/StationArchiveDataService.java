package cn.techstar.pvms.backend.module.stationarchive.service;

import cn.techstar.pvms.backend.module.stationarchive.repository.StationArchiveCompanyMapper;
import cn.techstar.pvms.backend.module.stationarchive.repository.StationArchiveCurveMapper;
import cn.techstar.pvms.backend.module.stationarchive.repository.StationArchiveInverterAlarmMapper;
import cn.techstar.pvms.backend.module.stationarchive.repository.StationArchiveInverterMapper;
import cn.techstar.pvms.backend.module.stationarchive.repository.StationArchiveStationMapper;
import cn.techstar.pvms.backend.module.stationarchive.repository.StationArchiveStrategyMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class StationArchiveDataService {

    private static final LocalDate DEFAULT_BIZ_DATE = LocalDate.of(2026, 3, 30);
    private static final String CURRENT_TIME_LABEL = "14:00";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Map<String, StatusMeta> STATUS_META = buildStatusMeta();
    private static final Map<String, Double> INVERTER_STATUS_FACTOR = Map.of(
        "normal", 1.00,
        "warning", 0.92,
        "fault", 0.08,
        "maintenance", 0.76,
        "offline", 0.00
    );
    private static final Map<String, Double> SUCCESS_RATE_BY_STATUS = Map.of(
        "normal", 98.4,
        "warning", 93.2,
        "fault", 84.8,
        "maintenance", 89.5,
        "offline", 76.0
    );

    private final StationArchiveCompanyMapper companyMapper;
    private final StationArchiveStationMapper stationMapper;
    private final StationArchiveCurveMapper curveMapper;
    private final StationArchiveInverterMapper inverterMapper;
    private final StationArchiveStrategyMapper strategyMapper;
    private final StationArchiveInverterAlarmMapper inverterAlarmMapper;
    private final StationArchiveSeriesService seriesService;

    public StationArchiveDataService(
        StationArchiveCompanyMapper companyMapper,
        StationArchiveStationMapper stationMapper,
        StationArchiveCurveMapper curveMapper,
        StationArchiveInverterMapper inverterMapper,
        StationArchiveStrategyMapper strategyMapper,
        StationArchiveInverterAlarmMapper inverterAlarmMapper,
        StationArchiveSeriesService seriesService
    ) {
        this.companyMapper = companyMapper;
        this.stationMapper = stationMapper;
        this.curveMapper = curveMapper;
        this.inverterMapper = inverterMapper;
        this.strategyMapper = strategyMapper;
        this.inverterAlarmMapper = inverterAlarmMapper;
        this.seriesService = seriesService;
    }

    public Map<String, Object> getArchiveList(String keyword, String gridStatus) {
        List<StationArchiveStationMapper.StationRow> stations = stationMapper.findAll();
        Map<String, List<StationArchiveInverterMapper.InverterRow>> invertersByStation = inverterMapper.findAll().stream()
            .collect(Collectors.groupingBy(StationArchiveInverterMapper.InverterRow::stationId, LinkedHashMap::new, Collectors.toList()));

        return orderedMap(
            "items",
            stations.stream()
                .filter(station -> matchesKeyword(station, keyword))
                .filter(station -> gridStatus == null || gridStatus.isBlank() || Objects.equals(station.gridStatus(), gridStatus))
                .map(station -> orderedMap(
                    "id", station.id(),
                    "name", station.name(),
                    "companyId", station.companyId(),
                    "companyName", station.companyName(),
                    "capacityKwp", round(station.capacityKwp(), 0),
                    "gridStatus", station.gridStatus(),
                    "gridStatusLabel", station.gridStatusLabel(),
                    "commissionDate", formatDate(station.commissionDate()),
                    "address", station.address(),
                    "inverterCount", invertersByStation.getOrDefault(station.id(), List.of()).size(),
                    "status", station.status()
                ))
                .toList()
        );
    }

    public Map<String, Object> getStationTree(String status) {
        List<StationArchiveCompanyMapper.CompanyRow> companies = companyMapper.findAll();
        List<StationArchiveStationMapper.StationRow> stations = stationMapper.findAll();
        Map<String, List<StationArchiveCurveMapper.CurveRow>> curvesByStation = curveMapper.findByDate(DEFAULT_BIZ_DATE).stream()
            .collect(Collectors.groupingBy(StationArchiveCurveMapper.CurveRow::stationId, LinkedHashMap::new, Collectors.toList()));
        Map<String, List<StationArchiveInverterMapper.InverterRow>> invertersByStation = inverterMapper.findAll().stream()
            .collect(Collectors.groupingBy(StationArchiveInverterMapper.InverterRow::stationId, LinkedHashMap::new, Collectors.toList()));

        List<Map<String, Object>> tree = new ArrayList<>();
        for (StationArchiveCompanyMapper.CompanyRow company : companies) {
            List<Map<String, Object>> children = stations.stream()
                .filter(station -> Objects.equals(station.companyId(), company.id()))
                .filter(station -> status == null || status.isBlank() || Objects.equals(station.status(), status))
                .map(station -> mapTreeStationNode(
                    station,
                    currentPoint(curvesByStation.getOrDefault(station.id(), List.of())),
                    invertersByStation.getOrDefault(station.id(), List.of())
                ))
                .toList();
            if (!children.isEmpty() || status == null || status.isBlank()) {
                tree.add(orderedMap(
                    "id", company.id(),
                    "label", company.name(),
                    "nodeType", "company",
                    "statusTag", "代理用户",
                    "statusColor", "#409EFF",
                    "children", children
                ));
            }
        }

        return orderedMap("tree", tree);
    }

    public Map<String, Object> getCompanyOverview(String companyId) {
        List<StationArchiveCompanyMapper.CompanyRow> companies = companyMapper.findAll();
        List<StationArchiveStationMapper.StationRow> stations = stationMapper.findAll();
        StationArchiveCompanyMapper.CompanyRow company = companies.stream()
            .filter(item -> Objects.equals(item.id(), companyId))
            .findFirst()
            .orElse(companies.getFirst());
        List<StationArchiveStationMapper.StationRow> companyStations = stations.stream()
            .filter(item -> Objects.equals(item.companyId(), company.id()))
            .sorted(Comparator.comparingInt(StationArchiveStationMapper.StationRow::sortIndex))
            .toList();
        Map<String, List<StationArchiveCurveMapper.CurveRow>> curvesByStation = curveMapper.findByDate(DEFAULT_BIZ_DATE).stream()
            .collect(Collectors.groupingBy(StationArchiveCurveMapper.CurveRow::stationId, LinkedHashMap::new, Collectors.toList()));

        List<Map<String, Object>> stationRows = companyStations.stream()
            .map(station -> buildCompanyStationRow(station, curvesByStation.getOrDefault(station.id(), List.of())))
            .toList();

        double totalCapacityMw = round(companyStations.stream().mapToDouble(StationArchiveStationMapper.StationRow::capacityKwp).sum() / 1000.0, 1);
        double realtimePowerMw = round(stationRows.stream().mapToDouble(item -> ((Number) item.get("realtimePowerKw")).doubleValue()).sum() / 1000.0, 2);
        double totalAdjustableKw = stationRows.stream().mapToDouble(item -> ((Number) item.get("adjustableKw")).doubleValue()).sum();
        double todayEnergyMwh = round(companyStations.stream()
            .mapToDouble(station -> seriesService.energyMwh(
                seriesService.build(curvesByStation.getOrDefault(station.id(), List.of()), "15min").pvOutputKw(),
                15
            ))
            .sum(), 1);
        long onlineStations = companyStations.stream().filter(station -> !"offline".equals(station.status())).count();
        double onlineRate = companyStations.isEmpty() ? 0 : round(onlineStations * 100.0 / companyStations.size(), 1);
        double todayPvRevenue = round(todayEnergyMwh * 1000.0 * 0.46, 0);
        double todayDispatchRevenue = round(Math.max(totalAdjustableKw, 0) * 0.18, 0);
        double monthlyPvRevenue = round(todayPvRevenue * 24.0, 0);
        double monthlyDispatchRevenue = round(todayDispatchRevenue * 22.0, 0);

        return orderedMap(
            "name", company.name(),
            "kpis", List.of(
                metric("capacity", "总装机", totalCapacityMw, "MWp", "el-icon-office-building", "teal"),
                metric("power", "实时功率", realtimePowerMw, "MW", "el-icon-lightning", "blue"),
                metric("energy", "当日发电", todayEnergyMwh, "MWh", "el-icon-data-analysis", "emerald"),
                metric("adjustable", "总可调空间", round(totalAdjustableKw / 1000.0, 2), "MW", "el-icon-sort", "orange"),
                metric("pvRevenue", "光伏收益(今日)", todayPvRevenue, "元", "el-icon-coin", "emerald"),
                metric("dispatchRevenue", "调控收益(今日)", todayDispatchRevenue, "元", "el-icon-money", "orange"),
                metric("monthlyPvRevenue", "本月光伏收益", monthlyPvRevenue, "元", "el-icon-coin", "teal"),
                metric("monthlyDispatchRevenue", "本月调控收益", monthlyDispatchRevenue, "元", "el-icon-money", "blue"),
                metric("online", "在线率", onlineRate, "%", "el-icon-success", "emerald")
            ),
            "stations", stationRows
        );
    }

    public Map<String, Object> getResourceOverview(String stationId) {
        StationArchiveStationMapper.StationRow station = resolveStation(stationId);
        StationArchiveSeriesService.SeriesGrid grid = resolveSeries(station.id(), DEFAULT_BIZ_DATE, "15min");
        List<StationArchiveInverterMapper.InverterRow> inverters = inverterMapper.findByStationId(station.id());
        Map<String, Object> stationRow = buildCompanyStationRow(
            station,
            curveMapper.findByStationIdAndDate(station.id(), DEFAULT_BIZ_DATE)
        );
        double realtimePowerKw = ((Number) stationRow.get("realtimePowerKw")).doubleValue();
        double loadKw = ((Number) stationRow.get("loadKw")).doubleValue();
        double adjustableKw = ((Number) stationRow.get("adjustableKw")).doubleValue();
        double todayEnergyMwh = seriesService.energyMwh(grid.pvOutputKw(), grid.stepMinutes());
        double forecastAccuracy = round(100.0 - averageDeviationRate(grid.pvOutputKw(), grid.forecastUltraShortKw()), 1);

        return orderedMap(
            "stationId", station.id(),
            "name", station.name(),
            "companyName", station.companyName(),
            "kpis", List.of(
                metric("capacity", "装机容量", round(station.capacityKwp() / 1000.0, 1), "MWp", "el-icon-office-building", "teal"),
                metric("realtime", "实时功率", realtimePowerKw, "kW", "el-icon-lightning", "blue"),
                metric("load", "当前负荷", loadKw, "kW", "el-icon-s-data", "orange"),
                metric("adjustable", "可调空间", adjustableKw, "kW", "el-icon-sort", "emerald"),
                metric("energy", "当日发电", todayEnergyMwh, "MWh", "el-icon-data-analysis", "teal"),
                metric("accuracy", "预测准确率", forecastAccuracy, "%", "el-icon-s-marketing", "blue")
            ),
            "powerCurve", orderedMap(
                "times", grid.times(),
                "series", List.of(
                    orderedMap("name", "日前预测", "data", grid.forecastDayAheadKw(), "type", "line"),
                    orderedMap("name", "超短期预测", "data", grid.forecastUltraShortKw(), "type", "line"),
                    orderedMap("name", "实际输出", "data", grid.pvOutputKw(), "type", "line")
                )
            ),
            "stations", List.of(stationRow),
            "inverters", inverters.stream().map(inverter -> orderedMap(
                "id", inverter.id(),
                "name", inverter.name(),
                "ratedPowerKw", inverter.ratedPowerKw(),
                "status", inverter.status()
            )).toList()
        );
    }

    public Map<String, Object> getStationRealtime(String stationId, String metric, LocalDate bizDate, String granularity) {
        StationArchiveStationMapper.StationRow station = resolveStation(stationId);
        StationArchiveSeriesService.SeriesGrid grid = resolveSeries(station.id(), resolveBizDate(bizDate), granularity);
        List<StationArchiveInverterMapper.InverterRow> inverters = inverterMapper.findByStationId(station.id());
        String resolvedMetric = metric == null || metric.isBlank() ? "adjustable" : metric;

        return switch (resolvedMetric) {
            case "pv-output" -> buildStationPvOutputPayload(station, grid, inverters);
            case "load" -> buildStationLoadPayload(station, grid);
            case "forecast" -> buildStationForecastPayload(grid);
            default -> buildStationAdjustablePayload(station, grid);
        };
    }

    public Map<String, Object> getStationAdjustable(String stationId) {
        StationArchiveStationMapper.StationRow station = resolveStation(stationId);
        StationArchiveSeriesService.SeriesGrid grid = resolveSeries(station.id(), DEFAULT_BIZ_DATE, "15min");
        List<Double> adjustableSeries = subtract(grid.loadKw(), grid.pvOutputKw());
        int currentIndex = seriesService.indexOfTime(grid, CURRENT_TIME_LABEL);
        double maxAdjustable = adjustableSeries.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double minAdjustable = adjustableSeries.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double currentAdjustable = adjustableSeries.get(currentIndex);

        return orderedMap(
            "stationId", station.id(),
            "currentPowerKw", grid.pvOutputKw().get(currentIndex),
            "maxAdjustableKw", round(maxAdjustable, 1),
            "minAdjustableKw", round(minAdjustable, 1),
            "adjustableRangeKw", round(maxAdjustable - minAdjustable, 1),
            "utilizationRate", maxAdjustable == 0 ? 0 : round(Math.abs(currentAdjustable) * 100.0 / Math.max(Math.abs(maxAdjustable), 1), 1),
            "periods", buildAdjustablePeriods(grid, adjustableSeries)
        );
    }

    public Map<String, Object> getStationStrategy(String stationId) {
        StationArchiveStrategyMapper.StrategyRow strategy = resolveStrategy(stationId);
        return orderedMap(
            "currentStrategy", orderedMap(
                "name", strategy.name(),
                "type", strategy.type(),
                "status", strategy.status(),
                "startTime", formatDateTime(strategy.startTime()),
                "endTime", formatDateTime(strategy.endTime()),
                "targetPowerKw", round(strategy.targetPowerKw(), 0),
                "estimatedRevenueCny", round(strategy.estimatedRevenueCny(), 0)
            ),
            "executionLogs", buildExecutionLogs(strategy)
        );
    }

    public Map<String, Object> getInverterRealtime(String inverterId) {
        List<StationArchiveInverterMapper.InverterRow> allInverters = inverterMapper.findAll();
        StationArchiveInverterMapper.InverterRow inverter = allInverters.stream()
            .filter(item -> Objects.equals(item.id(), inverterId))
            .findFirst()
            .orElse(allInverters.getFirst());
        StationArchiveStationMapper.StationRow station = resolveStation(inverter.stationId());
        StationArchiveSeriesService.SeriesGrid grid = resolveSeries(station.id(), DEFAULT_BIZ_DATE, "15min");
        List<StationArchiveInverterMapper.InverterRow> stationInverters = allInverters.stream()
            .filter(item -> Objects.equals(item.stationId(), station.id()))
            .sorted(Comparator.comparingInt(StationArchiveInverterMapper.InverterRow::sortIndex))
            .toList();
        List<InverterSeries> inverterSeries = buildInverterPowerSeries(stationInverters, grid.pvOutputKw());
        InverterSeries currentSeries = inverterSeries.stream()
            .filter(item -> Objects.equals(item.inverter().id(), inverter.id()))
            .findFirst()
            .orElse(inverterSeries.getFirst());
        int currentIndex = seriesService.indexOfTime(grid, CURRENT_TIME_LABEL);
        double currentPowerKw = currentSeries.data().get(currentIndex);
        double dailyEnergyMwh = seriesService.energyMwh(currentSeries.data(), grid.stepMinutes());

        return orderedMap(
            "inverterId", inverter.id(),
            "stationId", inverter.stationId(),
            "ratedPowerKw", round(inverter.ratedPowerKw(), 0),
            "realtimePowerKw", round(currentPowerKw, 1),
            "dailyEnergyMwh", round(dailyEnergyMwh, 2),
            "status", inverter.status(),
            "efficiency", resolveInverterEfficiency(inverter),
            "powerCurve", orderedMap(
                "times", grid.times(),
                "series", List.of(orderedMap("name", "逆变器功率", "data", currentSeries.data(), "type", "line"))
            ),
            "topology", orderedMap(
                "inverterName", inverter.name(),
                "ratedPowerKw", round(inverter.ratedPowerKw(), 0),
                "stringCount", inverter.stringCount(),
                "totalPanels", inverter.stringCount() * inverter.panelsPerString(),
                "strings", buildStringTopology(inverter, currentPowerKw)
            ),
            "alarms", inverterAlarmMapper.findByInverterId(inverter.id()).stream()
                .map(alarm -> orderedMap(
                    "time", formatDateTime(alarm.eventTime()),
                    "type", alarm.type(),
                    "level", alarm.level(),
                    "description", alarm.description(),
                    "status", alarm.status()
                ))
                .toList(),
            "deviceInfo", orderedMap(
                "model", inverter.model(),
                "manufacturer", inverter.manufacturer(),
                "sn", inverter.serialNo(),
                "firmwareVersion", inverter.firmwareVersion(),
                "installDate", formatDate(inverter.installDate()),
                "mpptChannels", inverter.mpptChannels(),
                "dcInputVoltage", round(inverter.dcInputVoltageV(), 1) + " V",
                "acOutputVoltage", round(inverter.acOutputVoltageV(), 0) + " V",
                "gridFrequency", round(inverter.gridFrequencyHz(), 1) + " Hz",
                "moduleTemperature", round(inverter.moduleTemperatureC(), 1) + " C",
                "ambientTemperature", round(inverter.ambientTemperatureC(), 1) + " C"
            )
        );
    }

    private Map<String, Object> mapTreeStationNode(
        StationArchiveStationMapper.StationRow station,
        StationArchiveCurveMapper.CurveRow currentCurve,
        List<StationArchiveInverterMapper.InverterRow> inverters
    ) {
        double loadKw = currentCurve == null ? station.loadBaseKw() : currentCurve.loadKw();
        double pvOutputKw = currentCurve == null ? 0 : currentCurve.pvOutputKw();
        double adjustableKw = round(loadKw - pvOutputKw, 1);

        return orderedMap(
            "id", station.id(),
            "label", station.name(),
            "nodeType", "station",
            "statusTag", "电站",
            "statusColor", "#67C23A",
            "extra", orderedMap(
                "capacityKwp", round(station.capacityKwp(), 0),
                "status", station.status(),
                "dataQuality", station.dataQuality(),
                "loadKw", round(loadKw, 1),
                "pvOutputKw", round(pvOutputKw, 1),
                "adjustableKw", adjustableKw
            ),
            "children", buildTreeInverters(inverters, pvOutputKw)
        );
    }

    private List<Map<String, Object>> buildTreeInverters(
        List<StationArchiveInverterMapper.InverterRow> inverters,
        double stationPvOutputKw
    ) {
        List<InverterSeries> series = buildInverterPowerSeries(inverters, List.of(stationPvOutputKw));
        return series.stream().map(item -> orderedMap(
            "id", item.inverter().id(),
            "label", item.inverter().name(),
            "nodeType", "inverter",
            "statusTag", "逆变器",
            "statusColor", "#E6A23C",
            "extra", orderedMap(
                "ratedPowerKw", round(item.inverter().ratedPowerKw(), 0),
                "realtimePowerKw", item.data().isEmpty() ? 0 : item.data().getFirst(),
                "status", item.inverter().status()
            )
        )).toList();
    }

    private Map<String, Object> buildCompanyStationRow(
        StationArchiveStationMapper.StationRow station,
        List<StationArchiveCurveMapper.CurveRow> curveRows
    ) {
        StationArchiveCurveMapper.CurveRow currentPoint = currentPoint(curveRows);
        double loadKw = currentPoint == null ? station.loadBaseKw() : currentPoint.loadKw();
        double realtimePowerKw = currentPoint == null ? 0 : currentPoint.pvOutputKw();
        return orderedMap(
            "id", station.id(),
            "name", station.name(),
            "capacityKwp", round(station.capacityKwp(), 0),
            "realtimePowerKw", round(realtimePowerKw, 1),
            "loadKw", round(loadKw, 1),
            "adjustableKw", round(loadKw - realtimePowerKw, 1),
            "status", station.status()
        );
    }

    private Map<String, Object> buildStationAdjustablePayload(
        StationArchiveStationMapper.StationRow station,
        StationArchiveSeriesService.SeriesGrid grid
    ) {
        List<Double> adjustableSeries = subtract(grid.loadKw(), grid.pvOutputKw());
        List<Double> upperLimit = grid.loadKw().stream().map(value -> round(value * 1.1, 1)).toList();
        List<Double> lowerLimit = grid.loadKw().stream().map(value -> round(Math.max(value * 0.3, 0), 1)).toList();
        int currentIndex = seriesService.indexOfTime(grid, CURRENT_TIME_LABEL);
        double currentAdjustable = adjustableSeries.get(currentIndex);
        double peakAdjustable = peakMagnitude(adjustableSeries);
        double todayEnergyMwh = seriesService.energyMwh(grid.pvOutputKw(), grid.stepMinutes());
        double todayPvRevenue = round(todayEnergyMwh * 1000.0 * (0.42 + station.sortIndex() * 0.004), 0);
        double todayDispatchRevenue = round(Math.abs(currentAdjustable) * (0.14 + station.sortIndex() * 0.002), 0);
        double monthlyPvRevenue = round(todayPvRevenue * 24.0, 0);
        double monthlyDispatchRevenue = round(todayDispatchRevenue * 22.0, 0);
        double cumulativePvRevenue = round(monthlyPvRevenue * 5.2, 0);
        double cumulativeDispatchRevenue = round(monthlyDispatchRevenue * 4.8, 0);
        double deviationRate = averageDeviationRate(grid.pvOutputKw(), grid.forecastUltraShortKw());

        return orderedMap(
            "times", grid.times(),
            "series", List.of(
                orderedMap("name", "负荷", "data", grid.loadKw(), "type", "line"),
                orderedMap("name", "光伏出力", "data", grid.pvOutputKw(), "type", "line"),
                orderedMap("name", "可调空间", "data", adjustableSeries, "type", "area"),
                orderedMap("name", "上限", "data", upperLimit, "type", "line"),
                orderedMap("name", "下限", "data", lowerLimit, "type", "line")
            ),
            "kpis", List.of(
                orderedMap("key", "currentAdjustable", "title", "当前可调空间", "value", round(currentAdjustable, 1), "unit", "kW"),
                orderedMap("key", "peakAdjustable", "title", "峰值可调空间", "value", round(peakAdjustable, 1), "unit", "kW"),
                orderedMap("key", "utilizationRate", "title", "可调利用率", "value", peakAdjustable == 0 ? 0 : round(Math.abs(currentAdjustable) * 100.0 / Math.max(Math.abs(peakAdjustable), 1), 1), "unit", "%")
            ),
            "stationKpis", orderedMap(
                "adjustableCapacityKw", round(currentAdjustable, 1),
                "todayPvRevenue", todayPvRevenue,
                "todayDispatchRevenue", todayDispatchRevenue,
                "monthlyPvRevenue", monthlyPvRevenue,
                "monthlyDispatchRevenue", monthlyDispatchRevenue,
                "cumulativePvRevenue", cumulativePvRevenue,
                "cumulativeDispatchRevenue", cumulativeDispatchRevenue
            ),
            "monthlyStats", orderedMap(
                "responseCount", 18 + station.sortIndex() * 2,
                "responseSuccessRate", SUCCESS_RATE_BY_STATUS.getOrDefault(station.status(), 90.0),
                "totalResponseEnergy", round(Math.abs(currentAdjustable) * 3.6, 0),
                "avgDeviationRate", round(deviationRate, 1),
                "peakShavingCount", 8 + station.sortIndex() % 9,
                "frequencyRegCount", 5 + station.sortIndex() % 7
            ),
            "fields", orderedMap(
                "deferrableLoad", round(station.loadBaseKw() * 0.35, 1),
                "maxUpCapacity", round(station.capacityKwp() * 0.08, 1),
                "maxDownCapacity", round(Math.max(currentAdjustable, 0) * 0.6, 1),
                "maxUpRate", round(seriesService.maxRampRate(upperLimit, grid.stepMinutes()), 1),
                "maxDownRate", round(seriesService.maxRampRate(lowerLimit, grid.stepMinutes()), 1),
                "maxUpTime", 15 + station.sortIndex() * 2,
                "maxDownTime", 20 + station.sortIndex(),
                "ratedPower", round(station.capacityKwp(), 0),
                "operatingCapacity", round(grid.pvOutputKw().get(currentIndex), 1),
                "essSOC", round(34 + station.sortIndex() * 2.1, 1)
            )
        );
    }

    private Map<String, Object> buildStationPvOutputPayload(
        StationArchiveStationMapper.StationRow station,
        StationArchiveSeriesService.SeriesGrid grid,
        List<StationArchiveInverterMapper.InverterRow> inverters
    ) {
        List<InverterSeries> inverterSeries = buildInverterPowerSeries(inverters, grid.pvOutputKw());
        List<Map<String, Object>> series = new ArrayList<>();
        series.add(orderedMap("name", "总出力", "data", grid.pvOutputKw(), "type", "line"));
        inverterSeries.forEach(item -> series.add(orderedMap("name", item.inverter().name(), "data", item.data(), "type", "line")));
        return orderedMap("times", grid.times(), "series", series);
    }

    private Map<String, Object> buildStationLoadPayload(
        StationArchiveStationMapper.StationRow station,
        StationArchiveSeriesService.SeriesGrid grid
    ) {
        List<Double> acData = new ArrayList<>(grid.loadKw().size());
        List<Double> lightingData = new ArrayList<>(grid.loadKw().size());
        List<Double> powerEquipmentData = new ArrayList<>(grid.loadKw().size());
        for (int index = 0; index < grid.loadKw().size(); index += 1) {
            double total = grid.loadKw().get(index);
            double acRatio = 0.40 + Math.sin((station.sortIndex() + index) / 8.0) * 0.02;
            double lightingRatio = 0.25 + Math.cos((station.sortIndex() + index) / 9.0) * 0.015;
            double ac = round(total * acRatio, 1);
            double lighting = round(total * lightingRatio, 1);
            double powerEquipment = round(Math.max(total - ac - lighting, 0), 1);
            acData.add(ac);
            lightingData.add(lighting);
            powerEquipmentData.add(powerEquipment);
        }

        return orderedMap(
            "times", grid.times(),
            "series", List.of(
                orderedMap("name", "总负荷", "data", grid.loadKw(), "type", "line"),
                orderedMap("name", "空调", "data", acData, "type", "area"),
                orderedMap("name", "照明", "data", lightingData, "type", "area"),
                orderedMap("name", "动力设备", "data", powerEquipmentData, "type", "area")
            )
        );
    }

    private Map<String, Object> buildStationForecastPayload(StationArchiveSeriesService.SeriesGrid grid) {
        return orderedMap(
            "times", grid.times(),
            "series", List.of(
                orderedMap("name", "日前预测", "data", grid.forecastDayAheadKw(), "type", "line"),
                orderedMap("name", "超短期预测", "data", grid.forecastUltraShortKw(), "type", "line"),
                orderedMap("name", "实际输出", "data", grid.pvOutputKw(), "type", "line")
            )
        );
    }

    private List<Map<String, Object>> buildAdjustablePeriods(
        StationArchiveSeriesService.SeriesGrid grid,
        List<Double> adjustableSeries
    ) {
        return List.of(
            period("00:00", "06:00", "valley", averageWithin(grid, adjustableSeries, 0, 24)),
            period("06:00", "08:00", "flat", averageWithin(grid, adjustableSeries, 24, 32)),
            period("08:00", "11:00", "peak", averageWithin(grid, adjustableSeries, 32, 44)),
            period("11:00", "13:00", "flat", averageWithin(grid, adjustableSeries, 44, 52)),
            period("13:00", "15:00", "peak", averageWithin(grid, adjustableSeries, 52, 60)),
            period("15:00", "17:00", "flat", averageWithin(grid, adjustableSeries, 60, 68)),
            period("17:00", "19:00", "peak", averageWithin(grid, adjustableSeries, 68, 76)),
            period("19:00", "24:00", "valley", averageWithin(grid, adjustableSeries, 76, 96))
        );
    }

    private Map<String, Object> period(String start, String end, String type, double adjustableKw) {
        return orderedMap(
            "start", start,
            "end", end,
            "type", type,
            "adjustableKw", round(adjustableKw, 1)
        );
    }

    private double averageWithin(
        StationArchiveSeriesService.SeriesGrid grid,
        List<Double> values,
        int startSlot,
        int endSlot
    ) {
        if (grid.stepMinutes() == 1) {
            int startMinute = startSlot * 15;
            int endMinute = endSlot * 15;
            return values.subList(startMinute, Math.min(endMinute, values.size())).stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);
        }
        return values.subList(startSlot, Math.min(endSlot, values.size())).stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0);
    }

    private List<Map<String, Object>> buildExecutionLogs(StationArchiveStrategyMapper.StrategyRow strategy) {
        if (Objects.equals(strategy.status(), "离线")) {
            return List.of(
                orderedMap("time", "10:30", "action", "尝试通信恢复", "result", "失败", "deviationRate", 0),
                orderedMap("time", "08:15", "action", "检测到通信中断", "result", "告警", "deviationRate", 0)
            );
        }
        if (Objects.equals(strategy.status(), "检修中")) {
            return List.of(
                orderedMap("time", "12:00", "action", "维持检修限额至 " + round(strategy.targetPowerKw(), 0) + "kW", "result", "成功", "deviationRate", 2.0),
                orderedMap("time", "08:00", "action", "进入检修模式", "result", "成功", "deviationRate", 0)
            );
        }
        if (Objects.equals(strategy.status(), "异常")) {
            return List.of(
                orderedMap("time", "14:00", "action", "降额至 " + round(strategy.targetPowerKw(), 0) + "kW", "result", "失败", "deviationRate", 12.4),
                orderedMap("time", "12:00", "action", "接收故障降额指令", "result", "成功", "deviationRate", 0),
                orderedMap("time", "08:00", "action", "降额至 " + round(strategy.targetPowerKw() + 400, 0) + "kW", "result", "偏差", "deviationRate", 8.6)
            );
        }
        if (Objects.equals(strategy.status(), "告警中")) {
            return List.of(
                orderedMap("time", "14:00", "action", "限功率至 " + round(strategy.targetPowerKw(), 0) + "kW", "result", "偏差", "deviationRate", 4.2),
                orderedMap("time", "12:30", "action", "接收限出指令", "result", "成功", "deviationRate", 0),
                orderedMap("time", "09:00", "action", "限功率至 " + round(strategy.targetPowerKw() + 250, 0) + "kW", "result", "成功", "deviationRate", 2.0)
            );
        }
        return List.of(
            orderedMap("time", "14:00", "action", "限功率至 " + round(strategy.targetPowerKw(), 0) + "kW", "result", "成功", "deviationRate", 1.2),
            orderedMap("time", "13:45", "action", "接收调度指令", "result", "成功", "deviationRate", 0),
            orderedMap("time", "10:30", "action", "限功率至 " + round(strategy.targetPowerKw() + 220, 0) + "kW", "result", "成功", "deviationRate", 2.1)
        );
    }

    private List<Map<String, Object>> buildStringTopology(
        StationArchiveInverterMapper.InverterRow inverter,
        double currentPowerKw
    ) {
        List<Map<String, Object>> strings = new ArrayList<>();
        double baseCurrent = inverter.stringCount() == 0 ? 0 : currentPowerKw / inverter.stringCount() / 60.0;
        for (int index = 1; index <= inverter.stringCount(); index += 1) {
            String status = "normal";
            if (Objects.equals(inverter.status(), "offline")) {
                status = "offline";
            } else if (Objects.equals(inverter.status(), "fault") && index == inverter.stringCount()) {
                status = "fault";
            }
            strings.add(orderedMap(
                "id", "STR-" + "%02d".formatted(index),
                "name", "组串 " + index,
                "panelCount", inverter.panelsPerString(),
                "currentA", round(baseCurrent * (0.92 + index * 0.03), 2),
                "voltageV", round(inverter.dcInputVoltageV() * (0.94 + index * 0.004), 1),
                "status", status
            ));
        }
        return strings;
    }

    private List<InverterSeries> buildInverterPowerSeries(
        List<StationArchiveInverterMapper.InverterRow> inverters,
        List<Double> stationOutputKw
    ) {
        if (inverters.isEmpty()) {
            return List.of();
        }
        List<Double> weights = inverters.stream()
            .map(inverter -> inverter.ratedPowerKw() * INVERTER_STATUS_FACTOR.getOrDefault(inverter.status(), 0.8))
            .toList();
        double totalWeight = weights.stream().mapToDouble(Double::doubleValue).sum();
        List<InverterSeries> result = new ArrayList<>();
        for (int inverterIndex = 0; inverterIndex < inverters.size(); inverterIndex += 1) {
            List<Double> data = new ArrayList<>(stationOutputKw.size());
            for (double stationPoint : stationOutputKw) {
                if (totalWeight <= 0) {
                    data.add(0.0);
                } else {
                    data.add(round(stationPoint * weights.get(inverterIndex) / totalWeight, 1));
                }
            }
            result.add(new InverterSeries(inverters.get(inverterIndex), data));
        }
        return result;
    }

    private StationArchiveStationMapper.StationRow resolveStation(String stationId) {
        List<StationArchiveStationMapper.StationRow> stations = stationMapper.findAll();
        return stations.stream()
            .filter(item -> Objects.equals(item.id(), stationId))
            .findFirst()
            .orElse(stations.getFirst());
    }

    private StationArchiveStrategyMapper.StrategyRow resolveStrategy(String stationId) {
        List<StationArchiveStrategyMapper.StrategyRow> strategies = strategyMapper.findAll();
        return strategies.stream()
            .filter(item -> Objects.equals(item.stationId(), stationId))
            .findFirst()
            .orElse(strategies.getFirst());
    }

    private StationArchiveSeriesService.SeriesGrid resolveSeries(String stationId, LocalDate bizDate, String granularity) {
        LocalDate resolvedDate = resolveBizDate(bizDate);
        List<StationArchiveCurveMapper.CurveRow> rows = curveMapper.findByStationIdAndDate(stationId, resolvedDate);
        if (rows.isEmpty() && !Objects.equals(resolvedDate, DEFAULT_BIZ_DATE)) {
            rows = curveMapper.findByStationIdAndDate(stationId, DEFAULT_BIZ_DATE);
        }
        return seriesService.build(rows, granularity);
    }

    private StationArchiveCurveMapper.CurveRow currentPoint(List<StationArchiveCurveMapper.CurveRow> curveRows) {
        return curveRows.stream()
            .filter(item -> item.timeSlot() == 56)
            .findFirst()
            .orElse(curveRows.isEmpty() ? null : curveRows.get(Math.min(curveRows.size() - 1, 56)));
    }

    private boolean matchesKeyword(StationArchiveStationMapper.StationRow station, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        return station.id().contains(keyword)
            || station.name().contains(keyword);
    }

    private List<Double> subtract(List<Double> left, List<Double> right) {
        List<Double> result = new ArrayList<>(left.size());
        for (int index = 0; index < Math.min(left.size(), right.size()); index += 1) {
            result.add(round(left.get(index) - right.get(index), 1));
        }
        return result;
    }

    private double peakMagnitude(List<Double> values) {
        double peak = 0;
        for (double value : values) {
            if (Math.abs(value) > Math.abs(peak)) {
                peak = value;
            }
        }
        return peak;
    }

    private double averageDeviationRate(List<Double> actual, List<Double> forecast) {
        List<Double> deviations = new ArrayList<>();
        for (int index = 0; index < Math.min(actual.size(), forecast.size()); index += 1) {
            double forecastValue = forecast.get(index);
            if (forecastValue > 0) {
                deviations.add(Math.abs(actual.get(index) - forecastValue) / forecastValue * 100.0);
            }
        }
        return round(deviations.stream().mapToDouble(Double::doubleValue).average().orElse(0), 1);
    }

    private double resolveInverterEfficiency(StationArchiveInverterMapper.InverterRow inverter) {
        return switch (inverter.status()) {
            case "warning" -> 96.4;
            case "fault" -> 89.2;
            case "maintenance" -> 94.8;
            case "offline" -> 0;
            default -> round(98.6 - inverter.sortIndex() * 0.3, 1);
        };
    }

    private LocalDate resolveBizDate(LocalDate bizDate) {
        return bizDate == null ? DEFAULT_BIZ_DATE : bizDate;
    }

    private String formatDate(LocalDate value) {
        return value == null ? "" : DATE_FORMATTER.format(value);
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "" : DATE_TIME_FORMATTER.format(value);
    }

    private Map<String, Object> metric(String key, String title, Object value, String unit, String icon, String accent) {
        return orderedMap(
            "key", key,
            "title", title,
            "value", value,
            "unit", unit,
            "icon", icon,
            "accent", accent
        );
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

    private record InverterSeries(
        StationArchiveInverterMapper.InverterRow inverter,
        List<Double> data
    ) {
    }
}
