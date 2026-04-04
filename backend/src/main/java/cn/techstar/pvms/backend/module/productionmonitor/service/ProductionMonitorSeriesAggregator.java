package cn.techstar.pvms.backend.module.productionmonitor.service;

import cn.techstar.pvms.backend.module.productionmonitor.repository.ProductionMonitorCurveMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProductionMonitorSeriesAggregator {

    public List<AggregatedPoint> aggregate(List<ProductionMonitorCurveMapper.CurveRow> rows, String granularity) {
        int bucketSize = resolveBucketSize(granularity);
        int stepMinutes = bucketSize * 15;
        Map<Integer, BucketAccumulator> buckets = new LinkedHashMap<>();

        rows.forEach(row -> {
            int bucketIndex = row.timeSlot() / bucketSize;
            BucketAccumulator accumulator = buckets.computeIfAbsent(bucketIndex, ignored -> new BucketAccumulator(bucketIndex * bucketSize));
            accumulator.loadKw += row.loadKw();
            accumulator.pvPowerKw += row.pvPowerKw();
            accumulator.forecastPowerKw += row.forecastPowerKw();
            accumulator.baselinePowerKw += row.baselinePowerKw();
            accumulator.irradianceSum += row.irradiance();
            accumulator.temperatureSum += row.temperature();
            accumulator.rowCount += 1;
        });

        return buckets.values().stream()
            .sorted(Comparator.comparingInt(item -> item.startSlot))
            .map(item -> new AggregatedPoint(
                formatTime(item.startSlot),
                round(item.loadKw, 1),
                round(item.pvPowerKw, 1),
                round(item.forecastPowerKw, 1),
                round(item.baselinePowerKw, 1),
                (int) Math.round(item.irradianceSum / Math.max(item.rowCount, 1)),
                round(item.temperatureSum / Math.max(item.rowCount, 1), 1),
                stepMinutes
            ))
            .toList();
    }

    public double calculateMaxRampRate(List<AggregatedPoint> points) {
        if (points.size() < 2) {
            return 0;
        }
        double maxRampRate = 0;
        for (int index = 1; index < points.size(); index += 1) {
            double previousGridKw = points.get(index - 1).loadKw() - points.get(index - 1).pvPowerKw();
            double currentGridKw = points.get(index).loadKw() - points.get(index).pvPowerKw();
            double rampRate = Math.abs(currentGridKw - previousGridKw) / points.get(index).stepMinutes();
            maxRampRate = Math.max(maxRampRate, rampRate);
        }
        return round(maxRampRate, 1);
    }

    public List<Double> toMwSeries(List<AggregatedPoint> points, java.util.function.ToDoubleFunction<AggregatedPoint> extractor) {
        List<Double> values = new ArrayList<>(points.size());
        points.forEach(point -> values.add(round(extractor.applyAsDouble(point) / 1000.0, 2)));
        return values;
    }

    public List<Double> toKwSeries(List<AggregatedPoint> points, java.util.function.ToDoubleFunction<AggregatedPoint> extractor) {
        List<Double> values = new ArrayList<>(points.size());
        points.forEach(point -> values.add(round(extractor.applyAsDouble(point), 1)));
        return values;
    }

    public List<Integer> toIntegerSeries(List<AggregatedPoint> points, java.util.function.ToIntFunction<AggregatedPoint> extractor) {
        List<Integer> values = new ArrayList<>(points.size());
        points.forEach(point -> values.add(extractor.applyAsInt(point)));
        return values;
    }

    public List<String> axis(List<AggregatedPoint> points) {
        return points.stream().map(AggregatedPoint::label).toList();
    }

    private int resolveBucketSize(String granularity) {
        return switch (granularity) {
            case "60m" -> 4;
            case "30m" -> 2;
            default -> 1;
        };
    }

    private String formatTime(int startSlot) {
        int totalMinutes = startSlot * 15;
        int hour = totalMinutes / 60;
        int minute = totalMinutes % 60;
        return "%02d:%02d".formatted(hour, minute);
    }

    private double round(double value, int scale) {
        double factor = Math.pow(10, scale);
        return Math.round(value * factor) / factor;
    }

    public record AggregatedPoint(
        String label,
        double loadKw,
        double pvPowerKw,
        double forecastPowerKw,
        double baselinePowerKw,
        int irradiance,
        double temperature,
        int stepMinutes
    ) {
    }

    private static final class BucketAccumulator {
        private final int startSlot;
        private double loadKw;
        private double pvPowerKw;
        private double forecastPowerKw;
        private double baselinePowerKw;
        private double irradianceSum;
        private double temperatureSum;
        private int rowCount;

        private BucketAccumulator(int startSlot) {
            this.startSlot = startSlot;
        }
    }
}
