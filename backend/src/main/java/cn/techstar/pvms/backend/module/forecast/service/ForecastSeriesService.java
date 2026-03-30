package cn.techstar.pvms.backend.module.forecast.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ForecastSeriesService {

    public static final int SLOTS_PER_DAY = 96;
    public static final int CURRENT_SLOT = 56;

    public List<String> build15mAxis() {
        List<String> axis = new ArrayList<>(SLOTS_PER_DAY);
        for (int slot = 0; slot < SLOTS_PER_DAY; slot += 1) {
            axis.add(labelForSlot(slot));
        }
        return axis;
    }

    public List<String> buildHourAxis() {
        List<String> axis = new ArrayList<>(24);
        for (int hour = 0; hour < 24; hour += 1) {
            axis.add(String.format("%02d", hour));
        }
        return axis;
    }

    public String labelForSlot(int slot) {
        int boundedSlot = Math.max(slot, 0);
        if (boundedSlot >= SLOTS_PER_DAY) {
            return "24:00";
        }
        int hour = boundedSlot / 4;
        int minute = (boundedSlot % 4) * 15;
        return String.format("%02d:%02d", hour, minute);
    }

    public String windowEndLabel(int endSlot) {
        return labelForSlot(Math.min(endSlot + 1, SLOTS_PER_DAY));
    }

    public List<Double> buildSeries(Map<Integer, Double> slotValues) {
        List<Double> series = new ArrayList<>(SLOTS_PER_DAY);
        for (int slot = 0; slot < SLOTS_PER_DAY; slot += 1) {
            series.add(round(slotValues.getOrDefault(slot, 0.0), 1));
        }
        return series;
    }

    public List<Double> toHourlyAverage(List<Double> values) {
        List<Double> hourly = new ArrayList<>(24);
        for (int hour = 0; hour < 24; hour += 1) {
            int start = hour * 4;
            int end = Math.min(start + 4, values.size());
            hourly.add(round(averageRange(values, start, end), 2));
        }
        return hourly;
    }

    public double averageRange(List<Double> values, int startInclusive, int endExclusive) {
        if (values.isEmpty() || startInclusive >= values.size() || startInclusive >= endExclusive) {
            return 0;
        }
        double sum = 0;
        int count = 0;
        for (int index = Math.max(startInclusive, 0); index < Math.min(endExclusive, values.size()); index += 1) {
            sum += values.get(index);
            count += 1;
        }
        return count == 0 ? 0 : sum / count;
    }

    public double round(double value, int scale) {
        double factor = Math.pow(10, scale);
        return Math.round(value * factor) / factor;
    }
}
