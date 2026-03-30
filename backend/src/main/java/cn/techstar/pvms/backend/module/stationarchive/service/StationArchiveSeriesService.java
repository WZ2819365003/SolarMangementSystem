package cn.techstar.pvms.backend.module.stationarchive.service;

import cn.techstar.pvms.backend.module.stationarchive.repository.StationArchiveCurveRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class StationArchiveSeriesService {

    public SeriesGrid build(List<StationArchiveCurveRepository.CurveRow> rows, String granularity) {
        List<StationArchiveCurveRepository.CurveRow> sortedRows = rows.stream()
            .sorted(Comparator.comparingInt(StationArchiveCurveRepository.CurveRow::timeSlot))
            .toList();

        List<Double> loadKw = sortedRows.stream().map(StationArchiveCurveRepository.CurveRow::loadKw).toList();
        List<Double> pvOutputKw = sortedRows.stream().map(StationArchiveCurveRepository.CurveRow::pvOutputKw).toList();
        List<Double> forecastDayAheadKw = sortedRows.stream().map(StationArchiveCurveRepository.CurveRow::forecastDayAheadKw).toList();
        List<Double> forecastUltraShortKw = sortedRows.stream().map(StationArchiveCurveRepository.CurveRow::forecastUltraShortKw).toList();
        String normalizedGranularity = normalizeGranularity(granularity);

        if ("1min".equals(normalizedGranularity)) {
            return new SeriesGrid(
                buildAxis(1, 24 * 60),
                interpolateToMinute(loadKw),
                interpolateToMinute(pvOutputKw),
                interpolateToMinute(forecastDayAheadKw),
                interpolateToMinute(forecastUltraShortKw),
                1
            );
        }

        return new SeriesGrid(
            buildAxis(15, sortedRows.size()),
            roundSeries(loadKw),
            roundSeries(pvOutputKw),
            roundSeries(forecastDayAheadKw),
            roundSeries(forecastUltraShortKw),
            15
        );
    }

    public String normalizeGranularity(String granularity) {
        return switch (granularity) {
            case "1m", "1min" -> "1min";
            default -> "15min";
        };
    }

    public double energyMwh(List<Double> kwSeries, int stepMinutes) {
        return round(kwSeries.stream().mapToDouble(Double::doubleValue).sum() * stepMinutes / 60.0 / 1000.0, 2);
    }

    public double maxRampRate(List<Double> kwSeries, int stepMinutes) {
        if (kwSeries.size() < 2) {
            return 0;
        }
        double maxRampRate = 0;
        for (int index = 1; index < kwSeries.size(); index += 1) {
            maxRampRate = Math.max(maxRampRate, Math.abs(kwSeries.get(index) - kwSeries.get(index - 1)) / stepMinutes);
        }
        return round(maxRampRate, 1);
    }

    public int indexOfTime(SeriesGrid grid, String label) {
        int index = grid.times().indexOf(label);
        if (index >= 0) {
            return index;
        }
        if (grid.stepMinutes() == 1) {
            return 14 * 60;
        }
        return Math.min(grid.times().size() - 1, 56);
    }

    private List<Double> roundSeries(List<Double> values) {
        return values.stream().map(value -> round(value, 1)).toList();
    }

    private List<Double> interpolateToMinute(List<Double> source) {
        List<Double> values = new ArrayList<>(24 * 60);
        for (int minute = 0; minute < 24 * 60; minute += 1) {
            int baseIndex = Math.min(source.size() - 1, minute / 15);
            int nextIndex = Math.min(source.size() - 1, baseIndex + 1);
            double fraction = (minute % 15) / 15.0;
            double start = source.get(baseIndex);
            double end = source.get(nextIndex);
            values.add(round(start + (end - start) * fraction, 1));
        }
        return values;
    }

    private List<String> buildAxis(int stepMinutes, int count) {
        List<String> axis = new ArrayList<>(count);
        for (int index = 0; index < count; index += 1) {
            int totalMinutes = index * stepMinutes;
            int hour = totalMinutes / 60;
            int minute = totalMinutes % 60;
            axis.add("%02d:%02d".formatted(hour, minute));
        }
        return axis;
    }

    private double round(double value, int scale) {
        double factor = Math.pow(10, scale);
        return Math.round(value * factor) / factor;
    }

    public record SeriesGrid(
        List<String> times,
        List<Double> loadKw,
        List<Double> pvOutputKw,
        List<Double> forecastDayAheadKw,
        List<Double> forecastUltraShortKw,
        int stepMinutes
    ) {
    }
}
