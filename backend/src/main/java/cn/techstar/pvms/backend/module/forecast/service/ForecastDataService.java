package cn.techstar.pvms.backend.module.forecast.service;

import cn.techstar.pvms.backend.module.forecast.repository.ForecastAdjustableWindowRepository;
import cn.techstar.pvms.backend.module.forecast.repository.ForecastCurveRepository;
import cn.techstar.pvms.backend.module.forecast.repository.ForecastErrorSampleRepository;
import cn.techstar.pvms.backend.module.forecast.repository.ForecastModelRepository;
import cn.techstar.pvms.backend.module.forecast.repository.ForecastMonthlyAccuracyRepository;
import cn.techstar.pvms.backend.module.forecast.repository.ForecastPredictionRepository;
import cn.techstar.pvms.backend.module.forecast.repository.ForecastStationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ForecastDataService {

    private static final LocalDate DEFAULT_BIZ_DATE = LocalDate.of(2026, 3, 30);
    private static final DateTimeFormatter TREND_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");
    private static final Map<String, StatusMeta> STATUS_META = Map.of(
        "normal", new StatusMeta("正常", "success"),
        "warning", new StatusMeta("预警", "warning"),
        "fault", new StatusMeta("故障", "danger"),
        "maintenance", new StatusMeta("检修", "info"),
        "offline", new StatusMeta("离线", "info")
    );

    private final ForecastStationRepository stationRepository;
    private final ForecastCurveRepository curveRepository;
    private final ForecastPredictionRepository predictionRepository;
    private final ForecastModelRepository modelRepository;
    private final ForecastErrorSampleRepository errorSampleRepository;
    private final ForecastMonthlyAccuracyRepository monthlyAccuracyRepository;
    private final ForecastAdjustableWindowRepository adjustableWindowRepository;
    private final ForecastSeriesService seriesService;
    private final ForecastMetricsCalculator metricsCalculator;

    public ForecastDataService(
        ForecastStationRepository stationRepository,
        ForecastCurveRepository curveRepository,
        ForecastPredictionRepository predictionRepository,
        ForecastModelRepository modelRepository,
        ForecastErrorSampleRepository errorSampleRepository,
        ForecastMonthlyAccuracyRepository monthlyAccuracyRepository,
        ForecastAdjustableWindowRepository adjustableWindowRepository,
        ForecastSeriesService seriesService,
        ForecastMetricsCalculator metricsCalculator
    ) {
        this.stationRepository = stationRepository;
        this.curveRepository = curveRepository;
        this.predictionRepository = predictionRepository;
        this.modelRepository = modelRepository;
        this.errorSampleRepository = errorSampleRepository;
        this.monthlyAccuracyRepository = monthlyAccuracyRepository;
        this.adjustableWindowRepository = adjustableWindowRepository;
        this.seriesService = seriesService;
        this.metricsCalculator = metricsCalculator;
    }

    public Map<String, Object> getMeta() {
        List<ForecastStationRepository.StationRow> stations = stationRepository.findAll();
        LinkedHashSet<String> regions = stations.stream()
            .map(ForecastStationRepository.StationRow::region)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        return orderedMap(
            "defaultDate", DEFAULT_BIZ_DATE.toString(),
            "defaultStationId", stations.isEmpty() ? "" : stations.getFirst().id(),
            "regions", new ArrayList<>(regions),
            "stations", stations.stream().map(this::mapStationMeta).toList(),
            "forecastTypes", List.of(
                orderedMap("label", "日前预测", "value", "day-ahead"),
                orderedMap("label", "超短期预测", "value", "ultra-short")
            ),
            "models", modelRepository.findAll().stream()
                .map(model -> orderedMap(
                    "id", model.id(),
                    "name", model.name(),
                    "type", model.type(),
                    "version", model.version(),
                    "provider", model.provider(),
                    "description", model.description(),
                    "status", model.status()
                ))
                .toList()
        );
    }

    public Map<String, Object> getOverview(String region, String stationId, String forecastType) {
        List<ForecastStationRepository.StationRow> filteredStations = fallbackStations(resolveStations(region, stationId));
        Set<String> stationIds = stationIdSet(filteredStations);
        String selectedForecastType = normalizeForecastType(forecastType);

        List<ForecastCurveRepository.CurveRow> curves = curveRepository.findByDate(DEFAULT_BIZ_DATE).stream()
            .filter(item -> stationIds.contains(item.stationId()))
            .toList();
        List<ForecastPredictionRepository.PredictionRow> predictions = predictionRepository.findByDate(DEFAULT_BIZ_DATE).stream()
            .filter(item -> stationIds.contains(item.stationId()))
            .toList();

        Map<String, List<ForecastCurveRepository.CurveRow>> curvesByStation = groupByStation(curves, ForecastCurveRepository.CurveRow::stationId);
        Map<String, Map<String, Map<Integer, ForecastPredictionRepository.PredictionRow>>> predictionsByStation =
            groupPredictionsByStationAndType(predictions);

        List<Double> aggregatedActual = emptySeries();
        List<Double> aggregatedDayAhead = emptySeries();
        List<Double> aggregatedUltraShort = emptySeries();
        List<Map<String, Object>> stationTable = new ArrayList<>();

        for (ForecastStationRepository.StationRow station : filteredStations) {
            List<Double> actualSeries = buildActualSeries(curvesByStation.getOrDefault(station.id(), List.of()));
            List<Double> dayAheadSeries = buildPredictionSeries(predictionsByStation, station.id(), "day-ahead");
            List<Double> ultraShortSeries = buildPredictionSeries(predictionsByStation, station.id(), "ultra-short");
            List<Double> selectedSeries = Objects.equals(selectedForecastType, "ultra-short") ? ultraShortSeries : dayAheadSeries;

            addInto(aggregatedActual, actualSeries);
            addInto(aggregatedDayAhead, dayAheadSeries);
            addInto(aggregatedUltraShort, ultraShortSeries);

            double predicted = selectedSeries.get(ForecastSeriesService.CURRENT_SLOT);
            double actual = actualSeries.get(ForecastSeriesService.CURRENT_SLOT);

            stationTable.add(orderedMap(
                "stationId", station.id(),
                "name", station.name(),
                "predicted", round(predicted, 2),
                "actual", round(actual, 2),
                "deviation", round(predicted - actual, 2),
                "accuracy", metricsCalculator.accuracyPercent(actualSeries, selectedSeries)
            ));
        }

        List<Double> selectedAggregate = Objects.equals(selectedForecastType, "ultra-short")
            ? aggregatedUltraShort
            : aggregatedDayAhead;

        return orderedMap(
            "bizDate", DEFAULT_BIZ_DATE.toString(),
            "forecastType", selectedForecastType,
            "kpis", orderedMap(
                "dayAheadAccuracy", metricsCalculator.accuracyPercent(aggregatedActual, aggregatedDayAhead),
                "ultraShortAccuracy", metricsCalculator.accuracyPercent(aggregatedActual, aggregatedUltraShort),
                "dailyDeviation", round(metricsCalculator.meanAbsoluteErrorKw(aggregatedActual, selectedAggregate) / 1000.0, 2),
                "qualifiedRate", metricsCalculator.qualifiedRate(aggregatedActual, selectedAggregate, 12.0)
            ),
            "stationTable", stationTable
        );
    }

    public Map<String, Object> getComparison(String region, String stationId) {
        List<ForecastStationRepository.StationRow> filteredStations = fallbackStations(resolveStations(region, stationId));
        ForecastStationRepository.StationRow focusStation = resolveFocusStation(filteredStations, stationId);

        List<ForecastCurveRepository.CurveRow> curveRows = curveRepository.findByDate(DEFAULT_BIZ_DATE).stream()
            .filter(item -> Objects.equals(item.stationId(), focusStation.id()))
            .toList();
        Map<String, Map<String, Map<Integer, ForecastPredictionRepository.PredictionRow>>> predictionsByStation =
            groupPredictionsByStationAndType(
                predictionRepository.findByDate(DEFAULT_BIZ_DATE).stream()
                    .filter(item -> Objects.equals(item.stationId(), focusStation.id()))
                    .toList()
            );

        return orderedMap(
            "stationId", focusStation.id(),
            "stationName", focusStation.name(),
            "timeLabels", seriesService.build15mAxis(),
            "series", orderedMap(
                "dayAhead", buildPredictionSeries(predictionsByStation, focusStation.id(), "day-ahead"),
                "ultraShort", buildPredictionSeries(predictionsByStation, focusStation.id(), "ultra-short"),
                "actual", buildActualSeries(curveRows)
            )
        );
    }

    public Map<String, Object> getDeviationHeatmap(
        String region,
        String stationId,
        String forecastType,
        LocalDate startDate,
        LocalDate endDate
    ) {
        List<ForecastStationRepository.StationRow> filteredStations = fallbackStations(resolveStations(region, stationId));
        Set<String> stationIds = stationIdSet(filteredStations);
        String selectedForecastType = normalizeForecastType(forecastType);
        LocalDate resolvedEndDate = endDate == null ? DEFAULT_BIZ_DATE : endDate;
        LocalDate resolvedStartDate = startDate == null ? resolvedEndDate.minusDays(6) : startDate;

        List<ForecastErrorSampleRepository.ErrorSampleRow> errorRows = errorSampleRepository
            .findByDateRange(resolvedStartDate, resolvedEndDate).stream()
            .filter(item -> stationIds.contains(item.stationId()))
            .filter(item -> Objects.equals(item.forecastType(), selectedForecastType))
            .toList();

        List<List<Double>> matrix = filteredStations.stream()
            .map(station -> buildHeatmapRow(station.id(), errorRows))
            .toList();

        return orderedMap(
            "forecastType", selectedForecastType,
            "startDate", resolvedStartDate.toString(),
            "endDate", resolvedEndDate.toString(),
            "hours", seriesService.buildHourAxis(),
            "stations", filteredStations.stream().map(ForecastStationRepository.StationRow::name).toList(),
            "data", matrix
        );
    }

    public Map<String, Object> getAdjustable(String region, String stationId, String forecastType) {
        List<ForecastStationRepository.StationRow> filteredStations = fallbackStations(resolveStations(region, stationId));
        Set<String> stationIds = stationIdSet(filteredStations);
        String selectedForecastType = normalizeForecastType(forecastType);

        Map<String, List<ForecastCurveRepository.CurveRow>> curvesByStation = groupByStation(
            curveRepository.findByDate(DEFAULT_BIZ_DATE).stream()
                .filter(item -> stationIds.contains(item.stationId()))
                .toList(),
            ForecastCurveRepository.CurveRow::stationId
        );
        Map<String, Map<String, Map<Integer, ForecastPredictionRepository.PredictionRow>>> predictionsByStation =
            groupPredictionsByStationAndType(
                predictionRepository.findByDate(DEFAULT_BIZ_DATE).stream()
                    .filter(item -> stationIds.contains(item.stationId()))
                    .toList()
            );
        Map<String, List<ForecastAdjustableWindowRepository.AdjustableWindowRow>> windowsByStation = groupByStation(
            adjustableWindowRepository.findByDate(DEFAULT_BIZ_DATE).stream()
                .filter(item -> stationIds.contains(item.stationId()))
                .toList(),
            ForecastAdjustableWindowRepository.AdjustableWindowRow::stationId
        );

        List<Double> aggregatedPredicted = emptySeries();
        List<Double> aggregatedUpper = emptySeries();
        List<Double> aggregatedLower = emptySeries();
        List<Map<String, Object>> timeline = new ArrayList<>();
        List<Map<String, Object>> stationTable = new ArrayList<>();

        for (ForecastStationRepository.StationRow station : filteredStations) {
            List<Double> loadSeries = buildLoadSeries(curvesByStation.getOrDefault(station.id(), List.of()));
            Map<Integer, ForecastPredictionRepository.PredictionRow> predictionRows = predictionsByStation
                .getOrDefault(station.id(), Map.of())
                .getOrDefault(selectedForecastType, Map.of());

            List<Double> predictedAdjustableSeries = emptySeries();
            List<Double> upperAdjustableSeries = emptySeries();
            List<Double> lowerAdjustableSeries = emptySeries();
            for (int slot = 0; slot < ForecastSeriesService.SLOTS_PER_DAY; slot += 1) {
                double load = loadSeries.get(slot);
                ForecastPredictionRepository.PredictionRow predictionRow = predictionRows.get(slot);
                double predictedPower = predictionRow == null ? 0 : predictionRow.predictedPowerKw() / 1000.0;
                double upperBoundPower = predictionRow == null ? 0 : predictionRow.upperBoundKw() / 1000.0;
                double lowerBoundPower = predictionRow == null ? 0 : predictionRow.lowerBoundKw() / 1000.0;

                predictedAdjustableSeries.set(slot, round(Math.max(load - predictedPower, 0), 2));
                upperAdjustableSeries.set(slot, round(Math.max(load - lowerBoundPower, 0), 2));
                lowerAdjustableSeries.set(slot, round(Math.max(load - upperBoundPower, 0), 2));
            }

            addInto(aggregatedPredicted, predictedAdjustableSeries);
            addInto(aggregatedUpper, upperAdjustableSeries);
            addInto(aggregatedLower, lowerAdjustableSeries);

            StatusMeta statusMeta = resolveStatusMeta(station.status());
            stationTable.add(orderedMap(
                "stationId", station.id(),
                "name", station.name(),
                "currentAdj", predictedAdjustableSeries.get(ForecastSeriesService.CURRENT_SLOT),
                "predicted4h", round(seriesService.averageRange(
                    predictedAdjustableSeries,
                    ForecastSeriesService.CURRENT_SLOT,
                    ForecastSeriesService.CURRENT_SLOT + 16
                ), 2),
                "maxUp", round(maxValue(upperAdjustableSeries), 2),
                "maxDown", round(maxValue(lowerAdjustableSeries), 2),
                "status", station.status(),
                "statusLabel", statusMeta.label(),
                "statusType", statusMeta.tagType()
            ));

            timeline.add(orderedMap(
                "stationId", station.id(),
                "name", station.name(),
                "windows", windowsByStation.getOrDefault(station.id(), List.of()).stream()
                    .map(window -> orderedMap(
                        "start", seriesService.labelForSlot(window.startSlot()),
                        "end", seriesService.windowEndLabel(window.endSlot()),
                        "status", window.windowStatus()
                    ))
                    .toList()
            ));
        }

        return orderedMap(
            "bizDate", DEFAULT_BIZ_DATE.toString(),
            "forecastType", selectedForecastType,
            "kpis", orderedMap(
                "totalAdjustable", round(aggregatedPredicted.get(ForecastSeriesService.CURRENT_SLOT), 2),
                "upAdjustable", round(aggregatedUpper.get(ForecastSeriesService.CURRENT_SLOT), 2),
                "downAdjustable", round(aggregatedLower.get(ForecastSeriesService.CURRENT_SLOT), 2),
                "max24h", round(maxValue(aggregatedPredicted), 2)
            ),
            "capacityCurve", orderedMap(
                "timeLabels", seriesService.build15mAxis(),
                "predicted", aggregatedPredicted,
                "upperBound", aggregatedUpper,
                "lowerBound", aggregatedLower
            ),
            "timeline", timeline,
            "stationTable", stationTable
        );
    }

    public Map<String, Object> getAccuracy(
        String region,
        String stationId,
        String forecastType,
        LocalDate startDate,
        LocalDate endDate
    ) {
        List<ForecastStationRepository.StationRow> filteredStations = fallbackStations(resolveStations(region, stationId));
        Set<String> stationIds = stationIdSet(filteredStations);
        String selectedForecastType = normalizeForecastType(forecastType);
        LocalDate resolvedEndDate = endDate == null ? DEFAULT_BIZ_DATE : endDate;
        LocalDate resolvedStartDate = startDate == null ? resolvedEndDate.minusDays(29) : startDate;

        List<ForecastErrorSampleRepository.ErrorSampleRow> errorRows = errorSampleRepository
            .findByDateRange(resolvedStartDate, resolvedEndDate).stream()
            .filter(item -> stationIds.contains(item.stationId()))
            .toList();
        List<ForecastErrorSampleRepository.ErrorSampleRow> selectedErrorRows = errorRows.stream()
            .filter(item -> Objects.equals(item.forecastType(), selectedForecastType))
            .toList();
        List<ForecastMonthlyAccuracyRepository.MonthlyAccuracyRow> monthlyRows = monthlyAccuracyRepository.findAll().stream()
            .filter(item -> stationIds.contains(item.stationId()))
            .filter(item -> Objects.equals(item.forecastType(), selectedForecastType))
            .toList();

        List<Double> errorValuesKw = selectedErrorRows.stream()
            .map(ForecastErrorSampleRepository.ErrorSampleRow::errorKw)
            .toList();
        List<Double> errorValuesMw = errorValuesKw.stream().map(value -> round(value / 1000.0, 3)).toList();
        List<String> trendDates = resolvedStartDate.datesUntil(resolvedEndDate.plusDays(1))
            .map(TREND_DATE_FORMATTER::format)
            .toList();

        List<Map<String, Object>> monthlyTable = buildMonthlyTable(monthlyRows);

        return orderedMap(
            "forecastType", selectedForecastType,
            "startDate", resolvedStartDate.toString(),
            "endDate", resolvedEndDate.toString(),
            "kpis", orderedMap(
                "mae", round(metricsCalculator.meanAbsoluteValue(errorValuesKw) / 1000.0, 3),
                "rmse", round(rmseFromErrorsKw(errorValuesKw) / 1000.0, 3),
                "qualifiedRate", qualifiedRateFromSamples(selectedErrorRows),
                "monthlyAvgAccuracy", monthlyTable.isEmpty()
                    ? 0
                    : round(((Number) monthlyTable.getLast().get("accuracy")).doubleValue(), 1)
            ),
            "trend", orderedMap(
                "dates", trendDates,
                "dayAheadAccuracy", buildTrendSeries(errorRows, resolvedStartDate, resolvedEndDate, "day-ahead"),
                "ultraShortAccuracy", buildTrendSeries(errorRows, resolvedStartDate, resolvedEndDate, "ultra-short")
            ),
            "distribution", orderedMap(
                "bins", buildDistributionLabels(-0.18, 0.18, 10),
                "counts", metricsCalculator.histogram(errorValuesMw, -0.18, 0.18, 10)
            ),
            "stationRanking", buildStationRanking(filteredStations, selectedErrorRows),
            "monthlyTable", monthlyTable
        );
    }

    private Map<String, Object> mapStationMeta(ForecastStationRepository.StationRow station) {
        return orderedMap(
            "id", station.id(),
            "name", station.name(),
            "region", station.region(),
            "companyName", station.companyName(),
            "status", station.status(),
            "dataQuality", station.dataQuality(),
            "capacityMw", round(station.capacityKwp() / 1000.0, 2)
        );
    }

    private List<ForecastStationRepository.StationRow> fallbackStations(List<ForecastStationRepository.StationRow> stations) {
        if (!stations.isEmpty()) {
            return stations;
        }
        return stationRepository.findAll();
    }

    private List<Double> buildHeatmapRow(
        String stationId,
        List<ForecastErrorSampleRepository.ErrorSampleRow> errorRows
    ) {
        List<Double> row = new ArrayList<>(24);
        for (int hour = 0; hour < 24; hour += 1) {
            final int targetHour = hour;
            List<Double> samples = errorRows.stream()
                .filter(item -> Objects.equals(item.stationId(), stationId))
                .filter(item -> item.hourSlot() == targetHour)
                .map(item -> Math.abs(item.errorKw()) / 1000.0)
                .toList();
            row.add(round(samples.stream().mapToDouble(Double::doubleValue).average().orElse(0), 2));
        }
        return row;
    }

    private List<ForecastStationRepository.StationRow> resolveStations(String region, String stationId) {
        List<ForecastStationRepository.StationRow> stations = stationRepository.findAll();
        return stations.stream()
            .filter(item -> region == null || region.isBlank() || Objects.equals(item.region(), region))
            .filter(item -> stationId == null || stationId.isBlank() || Objects.equals(item.id(), stationId))
            .toList();
    }

    private ForecastStationRepository.StationRow resolveFocusStation(
        List<ForecastStationRepository.StationRow> filteredStations,
        String stationId
    ) {
        if (!filteredStations.isEmpty()) {
            return filteredStations.stream()
                .filter(item -> stationId != null && !stationId.isBlank() && Objects.equals(item.id(), stationId))
                .findFirst()
                .orElse(filteredStations.getFirst());
        }
        return stationRepository.findAll().getFirst();
    }

    private Set<String> stationIdSet(Collection<ForecastStationRepository.StationRow> stations) {
        return stations.stream().map(ForecastStationRepository.StationRow::id).collect(Collectors.toSet());
    }

    private <T> Map<String, List<T>> groupByStation(Collection<T> rows, Function<T, String> stationIdExtractor) {
        return rows.stream().collect(Collectors.groupingBy(stationIdExtractor, LinkedHashMap::new, Collectors.toList()));
    }

    private Map<String, Map<String, Map<Integer, ForecastPredictionRepository.PredictionRow>>> groupPredictionsByStationAndType(
        Collection<ForecastPredictionRepository.PredictionRow> predictions
    ) {
        Map<String, Map<String, Map<Integer, ForecastPredictionRepository.PredictionRow>>> index = new LinkedHashMap<>();
        for (ForecastPredictionRepository.PredictionRow prediction : predictions) {
            index.computeIfAbsent(prediction.stationId(), key -> new LinkedHashMap<>())
                .computeIfAbsent(prediction.forecastType(), key -> new LinkedHashMap<>())
                .put(prediction.timeSlot(), prediction);
        }
        return index;
    }

    private Map<Integer, ForecastCurveRepository.CurveRow> indexCurves(List<ForecastCurveRepository.CurveRow> rows) {
        Map<Integer, ForecastCurveRepository.CurveRow> index = new LinkedHashMap<>();
        for (ForecastCurveRepository.CurveRow row : rows) {
            index.put(row.timeSlot(), row);
        }
        return index;
    }

    private List<Double> buildActualSeries(List<ForecastCurveRepository.CurveRow> rows) {
        return seriesService.buildSeries(rows.stream().collect(Collectors.toMap(
            ForecastCurveRepository.CurveRow::timeSlot,
            row -> row.pvOutputKw() / 1000.0,
            (left, right) -> right,
            LinkedHashMap::new
        )));
    }

    private List<Double> buildLoadSeries(List<ForecastCurveRepository.CurveRow> rows) {
        return seriesService.buildSeries(rows.stream().collect(Collectors.toMap(
            ForecastCurveRepository.CurveRow::timeSlot,
            row -> row.loadKw() / 1000.0,
            (left, right) -> right,
            LinkedHashMap::new
        )));
    }

    private List<Double> buildPredictionSeries(Map<Integer, ForecastPredictionRepository.PredictionRow> rows) {
        Map<Integer, Double> values = new LinkedHashMap<>();
        for (Map.Entry<Integer, ForecastPredictionRepository.PredictionRow> entry : rows.entrySet()) {
            values.put(entry.getKey(), entry.getValue().predictedPowerKw() / 1000.0);
        }
        return seriesService.buildSeries(values);
    }

    private List<Double> buildPredictionSeries(
        Map<String, Map<String, Map<Integer, ForecastPredictionRepository.PredictionRow>>> predictionsByStation,
        String stationId,
        String forecastType
    ) {
        Map<String, Map<Integer, ForecastPredictionRepository.PredictionRow>> stationPredictions =
            predictionsByStation.getOrDefault(stationId, Map.of());
        return buildPredictionSeries(stationPredictions.getOrDefault(forecastType, Map.of()));
    }

    private List<Double> emptySeries() {
        List<Double> series = new ArrayList<>(ForecastSeriesService.SLOTS_PER_DAY);
        for (int index = 0; index < ForecastSeriesService.SLOTS_PER_DAY; index += 1) {
            series.add(0.0);
        }
        return series;
    }

    private void addInto(List<Double> target, List<Double> values) {
        int size = Math.min(target.size(), values.size());
        for (int index = 0; index < size; index += 1) {
            target.set(index, round(target.get(index) + values.get(index), 3));
        }
    }

    private double maxValue(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).max().orElse(0);
    }

    private List<Double> buildTrendSeries(
        List<ForecastErrorSampleRepository.ErrorSampleRow> errorRows,
        LocalDate startDate,
        LocalDate endDate,
        String forecastType
    ) {
        List<Double> series = new ArrayList<>();
        for (LocalDate cursor = startDate; !cursor.isAfter(endDate); cursor = cursor.plusDays(1)) {
            LocalDate targetDate = cursor;
            List<Double> dailyErrors = errorRows.stream()
                .filter(item -> Objects.equals(item.bizDate(), targetDate))
                .filter(item -> Objects.equals(item.forecastType(), forecastType))
                .map(ForecastErrorSampleRepository.ErrorSampleRow::errorKw)
                .toList();
            series.add(metricsCalculator.accuracyFromErrorsKw(dailyErrors));
        }
        return series;
    }

    private List<String> buildDistributionLabels(double min, double max, int bins) {
        List<String> labels = new ArrayList<>(bins);
        double step = (max - min) / bins;
        for (int index = 0; index < bins; index += 1) {
            double start = min + step * index;
            double end = start + step;
            labels.add(String.format("%.2f~%.2f", start, end));
        }
        return labels;
    }

    private List<Map<String, Object>> buildStationRanking(
        List<ForecastStationRepository.StationRow> stations,
        List<ForecastErrorSampleRepository.ErrorSampleRow> errorRows
    ) {
        Map<String, List<ForecastErrorSampleRepository.ErrorSampleRow>> rowsByStation =
            groupByStation(errorRows, ForecastErrorSampleRepository.ErrorSampleRow::stationId);
        return stations.stream()
            .map(station -> orderedMap(
                "stationId", station.id(),
                "name", station.name(),
                "accuracy", metricsCalculator.accuracyFromErrorsKw(
                    rowsByStation.getOrDefault(station.id(), List.of()).stream()
                        .map(ForecastErrorSampleRepository.ErrorSampleRow::errorKw)
                        .toList()
                )
            ))
            .sorted((left, right) -> Double.compare(
                ((Number) right.get("accuracy")).doubleValue(),
                ((Number) left.get("accuracy")).doubleValue()
            ))
            .toList();
    }

    private List<Map<String, Object>> buildMonthlyTable(
        List<ForecastMonthlyAccuracyRepository.MonthlyAccuracyRow> rows
    ) {
        Map<String, List<ForecastMonthlyAccuracyRepository.MonthlyAccuracyRow>> rowsByMonth =
            rows.stream().collect(Collectors.groupingBy(
                ForecastMonthlyAccuracyRepository.MonthlyAccuracyRow::monthKey,
                LinkedHashMap::new,
                Collectors.toList()
            ));

        List<String> sortedMonths = rowsByMonth.keySet().stream().sorted().toList();
        List<Map<String, Object>> table = new ArrayList<>();
        double previousAccuracy = 0;
        for (String month : sortedMonths) {
            List<ForecastMonthlyAccuracyRepository.MonthlyAccuracyRow> monthRows = rowsByMonth.getOrDefault(month, List.of());
            double maeMw = monthRows.stream().mapToDouble(ForecastMonthlyAccuracyRepository.MonthlyAccuracyRow::maeKw).average().orElse(0) / 1000.0;
            double rmseMw = monthRows.stream().mapToDouble(ForecastMonthlyAccuracyRepository.MonthlyAccuracyRow::rmseKw).average().orElse(0) / 1000.0;
            double accuracy = monthRows.stream().mapToDouble(ForecastMonthlyAccuracyRepository.MonthlyAccuracyRow::accuracyPct).average().orElse(0);

            table.add(orderedMap(
                "month", month,
                "mae", round(maeMw, 3),
                "rmse", round(rmseMw, 3),
                "accuracy", round(accuracy, 1),
                "improvement", table.isEmpty() ? 0 : round(accuracy - previousAccuracy, 1)
            ));
            previousAccuracy = accuracy;
        }
        return table;
    }

    private double rmseFromErrorsKw(List<Double> errorValuesKw) {
        if (errorValuesKw.isEmpty()) {
            return 0;
        }
        double sum = 0;
        for (double errorKw : errorValuesKw) {
            sum += errorKw * errorKw;
        }
        return Math.sqrt(sum / errorValuesKw.size());
    }

    private double qualifiedRateFromSamples(List<ForecastErrorSampleRepository.ErrorSampleRow> rows) {
        if (rows.isEmpty()) {
            return 0;
        }
        long qualifiedCount = rows.stream().filter(ForecastErrorSampleRepository.ErrorSampleRow::qualified).count();
        return round(qualifiedCount * 100.0 / rows.size(), 1);
    }

    private String normalizeForecastType(String forecastType) {
        return Objects.equals(forecastType, "ultra-short") ? "ultra-short" : "day-ahead";
    }

    private StatusMeta resolveStatusMeta(String status) {
        return STATUS_META.getOrDefault(status, new StatusMeta("未知", "info"));
    }

    private double round(double value, int scale) {
        return seriesService.round(value, scale);
    }

    @SafeVarargs
    private final Map<String, Object> orderedMap(Object... keyValues) {
        Map<String, Object> ordered = new LinkedHashMap<>();
        for (int index = 0; index < keyValues.length; index += 2) {
            ordered.put((String) keyValues[index], keyValues[index + 1]);
        }
        return ordered;
    }

    private record StatusMeta(String label, String tagType) {
    }
}
