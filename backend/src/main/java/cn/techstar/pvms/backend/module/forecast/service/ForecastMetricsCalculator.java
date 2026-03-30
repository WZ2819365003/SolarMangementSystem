package cn.techstar.pvms.backend.module.forecast.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ForecastMetricsCalculator {

    public double meanAbsoluteErrorKw(List<Double> actual, List<Double> predicted) {
        int size = Math.min(actual.size(), predicted.size());
        if (size == 0) {
            return 0;
        }
        double sum = 0;
        for (int index = 0; index < size; index += 1) {
            sum += Math.abs(actual.get(index) - predicted.get(index));
        }
        return round(sum / size, 1);
    }

    public double rmseKw(List<Double> actual, List<Double> predicted) {
        int size = Math.min(actual.size(), predicted.size());
        if (size == 0) {
            return 0;
        }
        double sum = 0;
        for (int index = 0; index < size; index += 1) {
            double deviation = actual.get(index) - predicted.get(index);
            sum += deviation * deviation;
        }
        return round(Math.sqrt(sum / size), 1);
    }

    public double accuracyPercent(List<Double> actual, List<Double> predicted) {
        int size = Math.min(actual.size(), predicted.size());
        if (size == 0) {
            return 0;
        }
        double deviationRateSum = 0;
        for (int index = 0; index < size; index += 1) {
            deviationRateSum += relativeDeviationPercent(actual.get(index), predicted.get(index));
        }
        return round(Math.max(0, 100.0 - deviationRateSum / size), 1);
    }

    public double qualifiedRate(List<Double> actual, List<Double> predicted, double tolerancePercent) {
        int size = Math.min(actual.size(), predicted.size());
        if (size == 0) {
            return 0;
        }
        int qualifiedCount = 0;
        for (int index = 0; index < size; index += 1) {
            if (relativeDeviationPercent(actual.get(index), predicted.get(index)) <= tolerancePercent) {
                qualifiedCount += 1;
            }
        }
        return round(qualifiedCount * 100.0 / size, 1);
    }

    public double meanAbsoluteValue(List<Double> values) {
        if (values.isEmpty()) {
            return 0;
        }
        double sum = 0;
        for (double value : values) {
            sum += Math.abs(value);
        }
        return round(sum / values.size(), 1);
    }

    public double accuracyFromErrorsKw(List<Double> errorsKw) {
        double avgAbsKw = meanAbsoluteValue(errorsKw);
        return accuracyFromAverageAbsKw(avgAbsKw);
    }

    public double accuracyFromAverageAbsKw(double avgAbsKw) {
        return round(Math.max(80.0, Math.min(99.9, 100.0 - avgAbsKw / 12.0)), 1);
    }

    public List<Integer> histogram(List<Double> values, double min, double max, int bins) {
        List<Integer> counts = new ArrayList<>(bins);
        for (int index = 0; index < bins; index += 1) {
            counts.add(0);
        }
        if (bins <= 0 || max <= min) {
            return counts;
        }
        double bucketWidth = (max - min) / bins;
        for (double value : values) {
            if (value <= min) {
                counts.set(0, counts.getFirst() + 1);
                continue;
            }
            if (value >= max) {
                counts.set(bins - 1, counts.get(bins - 1) + 1);
                continue;
            }
            int bucket = (int) ((value - min) / bucketWidth);
            counts.set(bucket, counts.get(bucket) + 1);
        }
        return counts;
    }

    public double round(double value, int scale) {
        double factor = Math.pow(10, scale);
        return Math.round(value * factor) / factor;
    }

    private double relativeDeviationPercent(double actual, double predicted) {
        double denominator = Math.max(Math.max(Math.abs(actual), Math.abs(predicted)), 1.0);
        return Math.abs(actual - predicted) * 100.0 / denominator;
    }
}
