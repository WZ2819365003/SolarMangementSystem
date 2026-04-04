package cn.techstar.pvms.backend.module.strategy.service;

import cn.techstar.pvms.backend.module.forecast.repository.ForecastPredictionMapper;
import cn.techstar.pvms.backend.module.forecast.service.ForecastMetricsCalculator;
import cn.techstar.pvms.backend.module.forecast.service.ForecastSeriesService;
import cn.techstar.pvms.backend.module.stationarchive.repository.StationArchiveCurveMapper;
import cn.techstar.pvms.backend.module.strategy.model.StrategyRequest;
import cn.techstar.pvms.backend.module.strategy.repository.StrategyMetaMapper;
import cn.techstar.pvms.backend.module.strategy.repository.StrategyPriceMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class StrategySimulationService {

    private final StrategyMetaMapper metaMapper;
    private final StrategyPriceMapper priceMapper;
    private final StationArchiveCurveMapper stationArchiveCurveMapper;
    private final ForecastPredictionMapper forecastPredictionMapper;
    private final ForecastSeriesService forecastSeriesService;
    private final ForecastMetricsCalculator metricsCalculator;

    public StrategySimulationService(
        StrategyMetaMapper metaMapper,
        StrategyPriceMapper priceMapper,
        StationArchiveCurveMapper stationArchiveCurveMapper,
        ForecastPredictionMapper forecastPredictionMapper,
        ForecastSeriesService forecastSeriesService,
        ForecastMetricsCalculator metricsCalculator
    ) {
        this.metaMapper = metaMapper;
        this.priceMapper = priceMapper;
        this.stationArchiveCurveMapper = stationArchiveCurveMapper;
        this.forecastPredictionMapper = forecastPredictionMapper;
        this.forecastSeriesService = forecastSeriesService;
        this.metricsCalculator = metricsCalculator;
    }

    public SimulationResult simulate(StrategyRequest request) {
        StrategyMetaMapper.StationRow station = resolveStation(request.stationId());
        LocalDate bizDate = resolveBizDate(request);
        List<StationArchiveCurveMapper.CurveRow> curveRows =
            stationArchiveCurveMapper.findByStationIdAndDate(station.id(), bizDate);
        Map<Integer, ForecastPredictionMapper.PredictionRow> predictionsBySlot =
            forecastPredictionMapper.findByDate(bizDate).stream()
                .filter(item -> Objects.equals(item.stationId(), station.id()))
                .filter(item -> Objects.equals(item.forecastType(), "day-ahead"))
                .collect(java.util.stream.Collectors.toMap(
                    ForecastPredictionMapper.PredictionRow::timeSlot,
                    item -> item,
                    (left, right) -> left,
                    LinkedHashMap::new
                ));
        Map<Integer, StrategyPriceMapper.PricePeriodRow> priceBySlot = buildPriceBySlot(station.id());

        int startSlot = StrategySupport.slotOf(request.startTime());
        int endSlotExclusive = Math.min(96, Math.max(startSlot + 1, StrategySupport.endSlotExclusive(request.endTime())));
        double targetPowerKw = request.targetPowerKw() == null ? 0 : request.targetPowerKw();
        double priceFactor = StrategySupport.TYPE_PRICE_FACTOR.getOrDefault(request.type(), 0.85);
        double rewardFactor = StrategySupport.TYPE_REWARD_FACTOR.getOrDefault(request.type(), 0.16);
        double penaltyFactor = StrategySupport.TYPE_PENALTY_FACTOR.getOrDefault(request.type(), 0.09);

        double peakSaving = 0;
        double responseReward = 0;
        double penalty = 0;
        double expectedEnergyMwh = 0;
        List<Double> actualSeries = new ArrayList<>();
        List<Double> predictedSeries = new ArrayList<>();
        List<Map<String, Object>> timeline = new ArrayList<>();

        Map<Integer, StationArchiveCurveMapper.CurveRow> curveBySlot = curveRows.stream()
            .collect(java.util.stream.Collectors.toMap(
                StationArchiveCurveMapper.CurveRow::timeSlot,
                item -> item,
                (left, right) -> left,
                LinkedHashMap::new
            ));

        for (int slot = startSlot; slot < endSlotExclusive; slot += 1) {
            StationArchiveCurveMapper.CurveRow curve = curveBySlot.get(slot);
            ForecastPredictionMapper.PredictionRow prediction = predictionsBySlot.get(slot);
            StrategyPriceMapper.PricePeriodRow price = priceBySlot.get(slot);
            double loadKw = curve == null ? station.capacityKwp() * 0.42 : curve.loadKw();
            double actualPvKw = curve == null ? station.capacityKwp() * 0.33 : curve.pvOutputKw();
            double predictedKw = prediction == null ? actualPvKw : prediction.predictedPowerKw();
            double upperKw = prediction == null ? predictedKw * 1.08 : prediction.upperBoundKw();
            double lowerKw = prediction == null ? predictedKw * 0.92 : prediction.lowerBoundKw();
            double adjustableKw = Math.min(targetPowerKw, Math.max(loadKw - actualPvKw, 0));
            double forecastSupportKw = Math.min(targetPowerKw * 0.35, Math.max(predictedKw - actualPvKw, 0));
            double dispatchKw = Math.min(targetPowerKw, adjustableKw * 0.68 + forecastSupportKw * 0.32);
            double slotHours = 0.25;
            double priceValue = price == null ? 0.65 : price.priceCnyPerKwh();
            double deviationPct = Math.abs(predictedKw - actualPvKw) * 100.0 / Math.max(Math.max(predictedKw, actualPvKw), 1.0);

            peakSaving += dispatchKw * slotHours * priceValue * priceFactor;
            responseReward += dispatchKw * slotHours * rewardFactor;
            penalty += dispatchKw * slotHours * (deviationPct / 100.0) * penaltyFactor;
            expectedEnergyMwh += dispatchKw * slotHours / 1000.0;
            actualSeries.add(actualPvKw);
            predictedSeries.add(predictedKw);

            timeline.add(StrategySupport.orderedMap(
                "time", forecastSeriesService.labelForSlot(slot),
                "loadKw", StrategySupport.round(loadKw, 1),
                "actualPvKw", StrategySupport.round(actualPvKw, 1),
                "predictedPvKw", StrategySupport.round(predictedKw, 1),
                "upperBoundKw", StrategySupport.round(upperKw, 1),
                "lowerBoundKw", StrategySupport.round(lowerKw, 1),
                "dispatchKw", StrategySupport.round(dispatchKw, 1),
                "priceType", price == null ? "flat" : price.priceType(),
                "price", price == null ? 0.65 : StrategySupport.round(price.priceCnyPerKwh(), 3)
            ));
        }

        double estimatedRevenue = Math.max(0, peakSaving + responseReward - penalty);
        double accuracy = metricsCalculator.accuracyPercent(actualSeries, predictedSeries);
        double varianceRatio = Math.max(0.04, (100.0 - accuracy) / 100.0);
        double confidenceLow = Math.max(0, estimatedRevenue * (1 - varianceRatio * 0.8));
        double confidenceHigh = estimatedRevenue * (1 + varianceRatio * 0.6);
        double successProbability = Math.max(72.0, Math.min(98.5, accuracy - varianceRatio * 8 + 6));

        return new SimulationResult(
            station.id(),
            station.name(),
            request.type(),
            StrategySupport.round(estimatedRevenue, 2),
            StrategySupport.round(confidenceLow, 2),
            StrategySupport.round(confidenceHigh, 2),
            StrategySupport.round(successProbability, 1),
            StrategySupport.round(peakSaving, 2),
            StrategySupport.round(responseReward, 2),
            StrategySupport.round(penalty, 2),
            StrategySupport.round(expectedEnergyMwh, 3),
            timeline
        );
    }

    public Map<String, Object> simulatePayload(StrategyRequest request) {
        SimulationResult result = simulate(request);
        return StrategySupport.orderedMap(
            "stationId", result.stationId(),
            "stationName", result.stationName(),
            "type", result.type(),
            "estimatedRevenue", result.estimatedRevenue(),
            "confidenceRange", StrategySupport.orderedMap(
                "low", result.confidenceLow(),
                "high", result.confidenceHigh()
            ),
            "breakdown", StrategySupport.orderedMap(
                "peakSaving", result.peakSaving(),
                "responseReward", result.responseReward(),
                "penalty", result.penalty(),
                "expectedEnergyMwh", result.expectedEnergyMwh()
            ),
            "successProbability", result.successProbability(),
            "timeline", result.timeline()
        );
    }

    public Map<String, Object> batchSimulatePayload(List<StrategyRequest> requests) {
        List<Map<String, Object>> results = requests.stream()
            .map(request -> {
                SimulationResult result = simulate(request);
                return StrategySupport.orderedMap(
                    "stationId", result.stationId(),
                    "stationName", result.stationName(),
                    "type", result.type(),
                    "estimatedRevenue", result.estimatedRevenue(),
                    "confidenceRange", StrategySupport.orderedMap(
                        "low", result.confidenceLow(),
                        "high", result.confidenceHigh()
                    ),
                    "successProbability", result.successProbability()
                );
            })
            .toList();

        double totalRevenue = results.stream()
            .mapToDouble(item -> ((Number) item.get("estimatedRevenue")).doubleValue())
            .sum();

        return StrategySupport.orderedMap(
            "results", results,
            "totalRevenue", StrategySupport.round(totalRevenue, 2)
        );
    }

    private StrategyMetaMapper.StationRow resolveStation(String stationId) {
        return metaMapper.findStations().stream()
            .filter(item -> Objects.equals(item.id(), stationId))
            .findFirst()
            .orElse(metaMapper.findStations().stream().min(Comparator.comparingInt(StrategyMetaMapper.StationRow::sortIndex)).orElseThrow());
    }

    private LocalDate resolveBizDate(StrategyRequest request) {
        if (request.startTime() != null) {
            return request.startTime().toLocalDate();
        }
        if (request.endTime() != null) {
            return request.endTime().toLocalDate();
        }
        return StrategySupport.DEFAULT_BIZ_DATE;
    }

    private Map<Integer, StrategyPriceMapper.PricePeriodRow> buildPriceBySlot(String stationId) {
        Map<Integer, StrategyPriceMapper.PricePeriodRow> mapping = new LinkedHashMap<>();
        priceMapper.findByStationId(stationId).forEach(period -> {
            for (int slot = period.startSlot(); slot <= period.endSlot() && slot < 96; slot += 1) {
                mapping.put(slot, period);
            }
        });
        return mapping;
    }

    public record SimulationResult(
        String stationId,
        String stationName,
        String type,
        double estimatedRevenue,
        double confidenceLow,
        double confidenceHigh,
        double successProbability,
        double peakSaving,
        double responseReward,
        double penalty,
        double expectedEnergyMwh,
        List<Map<String, Object>> timeline
    ) {
    }
}
