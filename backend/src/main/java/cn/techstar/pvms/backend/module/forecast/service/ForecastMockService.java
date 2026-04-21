package cn.techstar.pvms.backend.module.forecast.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ForecastMockService {

    private static final List<StationRef> STATIONS = List.of(
        new StationRef("ST-001", "深圳港科园区光伏电站", 2500),
        new StationRef("ST-002", "松山湖智造园光伏电站", 6200),
        new StationRef("ST-007", "武汉物流基地光伏电站", 4800),
        new StationRef("ST-010", "合肥研发中心光伏电站", 1800),
        new StationRef("ST-014", "成都西部基地光伏电站", 5400)
    );

    public Map<String, Object> getMeta() {
        return orderedMap(
            "stations", STATIONS.stream().map(s -> orderedMap("id", s.id(), "name", s.name())).toList(),
            "models", List.of(
                orderedMap("id", "lstm", "name", "LSTM 神经网络"),
                orderedMap("id", "xgboost", "name", "XGBoost 集成学习"),
                orderedMap("id", "physical", "name", "物理模型")
            ),
            "granularityOptions", List.of(
                orderedMap("label", "15分钟", "value", "15m"),
                orderedMap("label", "30分钟", "value", "30m"),
                orderedMap("label", "1小时", "value", "60m")
            )
        );
    }

    public Map<String, Object> getOverview(String stationId) {
        double totalCapacity = stationId != null && !stationId.isBlank()
            ? STATIONS.stream().filter(s -> s.id().equals(stationId)).findFirst().map(StationRef::capacityKwp).orElse(2500.0)
            : STATIONS.stream().mapToDouble(StationRef::capacityKwp).sum();

        double todayForecast = Math.round(totalCapacity * 4.5);
        List<String> dates = new ArrayList<>();
        List<Double> accuracyValues = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            dates.add(LocalDate.of(2026, 3, 29 - i).toString());
            accuracyValues.add(93.5 + Math.random() * 5);
        }

        List<String> timeAxis = new ArrayList<>();
        List<Double> forecast = new ArrayList<>();
        List<Double> actual = new ArrayList<>();
        for (int i = 0; i < 96; i++) {
            double hour = i / 4.0;
            timeAxis.add(String.format("%02d:%02d", (int) hour, (i % 4) * 15));
            double solar = Math.max(0, Math.sin(((hour - 6) / 12) * Math.PI));
            forecast.add((double) Math.round(totalCapacity * solar * 0.75));
            actual.add((double) Math.round(totalCapacity * solar * 0.72 + Math.cos(i / 8.0) * totalCapacity * 0.02));
        }

        return orderedMap(
            "kpi", orderedMap(
                "todayForecastKwh", todayForecast,
                "accuracy", 96.2,
                "rmse", 128,
                "mae", 86
            ),
            "accuracyTrend", orderedMap("dates", dates, "values", accuracyValues),
            "forecastVsActual", orderedMap("timeAxis", timeAxis, "forecast", forecast, "actual", actual)
        );
    }

    public Map<String, Object> getAdjustable(String stationId) {
        double capacity = stationId != null && !stationId.isBlank()
            ? STATIONS.stream().filter(s -> s.id().equals(stationId)).findFirst().map(StationRef::capacityKwp).orElse(2500.0)
            : STATIONS.stream().mapToDouble(StationRef::capacityKwp).sum();

        List<Map<String, Object>> timeline = new ArrayList<>();
        for (int i = 0; i < 96; i++) {
            double hour = i / 4.0;
            double solar = Math.max(0, Math.sin(((hour - 6) / 12) * Math.PI));
            double forecastPower = Math.round(capacity * solar * 0.75);
            double load = Math.round(capacity * 0.3 * (0.6 + solar * 0.4));
            double adjustable = Math.max(0, Math.round(forecastPower - load));
            timeline.add(orderedMap(
                "time", String.format("%02d:%02d", (int) hour, (i % 4) * 15),
                "forecastPowerKw", forecastPower,
                "loadKw", load,
                "adjustableKw", adjustable
            ));
        }

        double maxAdj = timeline.stream().mapToDouble(m -> ((Number) m.get("adjustableKw")).doubleValue()).max().orElse(0);
        double avgAdj = Math.round(timeline.stream().mapToDouble(m -> ((Number) m.get("adjustableKw")).doubleValue()).average().orElse(0));

        return orderedMap(
            "summary", orderedMap("maxAdjustableKw", Math.round(maxAdj), "avgAdjustableKw", avgAdj),
            "timeline", timeline
        );
    }

    public Map<String, Object> getAccuracy(String stationId, String dateRange) {
        List<Map<String, Object>> byStation = STATIONS.stream().map(s -> {
            double acc = 93 + Math.random() * 6;
            return orderedMap("stationId", s.id(), "stationName", s.name(), "accuracy", Math.round(acc * 10) / 10.0);
        }).toList();

        List<Map<String, Object>> byHour = new ArrayList<>();
        for (int h = 5; h <= 19; h++) {
            double acc = h >= 9 && h <= 15 ? 95 + Math.random() * 4 : 85 + Math.random() * 10;
            byHour.add(orderedMap("hour", h, "accuracy", Math.round(acc * 10) / 10.0));
        }

        return orderedMap(
            "overall", orderedMap("accuracy", 96.2, "rmse", 128, "mae", 86, "mape", 3.8),
            "byStation", byStation,
            "byHour", byHour
        );
    }

    public Map<String, Object> getComparison(String stationId, String date) {
        double capacity = stationId != null && !stationId.isBlank()
            ? STATIONS.stream().filter(s -> s.id().equals(stationId)).findFirst().map(StationRef::capacityKwp).orElse(2500.0)
            : 2500.0;

        List<String> timeAxis = new ArrayList<>();
        List<Double> forecast = new ArrayList<>();
        List<Double> actual = new ArrayList<>();
        List<Double> deviation = new ArrayList<>();
        for (int i = 0; i < 96; i++) {
            double hour = i / 4.0;
            timeAxis.add(String.format("%02d:%02d", (int) hour, (i % 4) * 15));
            double solar = Math.max(0, Math.sin(((hour - 6) / 12) * Math.PI));
            double f = Math.round(capacity * solar * 0.75);
            double a = Math.round(capacity * solar * 0.72 + Math.cos(i / 8.0) * capacity * 0.02);
            forecast.add(f);
            actual.add(a);
            deviation.add(f > 0 ? Math.round((a - f) * 100.0 / f * 10) / 10.0 : 0);
        }

        return orderedMap(
            "stationId", stationId != null ? stationId : "ST-001",
            "date", date != null ? date : "2026-03-29",
            "timeAxis", timeAxis,
            "forecast", forecast,
            "actual", actual,
            "deviation", deviation
        );
    }

    public Map<String, Object> getDeviationHeatmap(String dateRange) {
        List<String> stations = STATIONS.stream().map(s -> s.name().substring(0, Math.min(4, s.name().length()))).toList();
        List<Integer> hours = new ArrayList<>();
        for (int h = 5; h <= 19; h++) hours.add(h);

        List<List<Double>> matrix = new ArrayList<>();
        for (int s = 0; s < stations.size(); s++) {
            List<Double> row = new ArrayList<>();
            for (int h = 5; h <= 19; h++) {
                double dev = (h >= 9 && h <= 15) ? 1 + Math.random() * 4 : 3 + Math.random() * 8;
                row.add(Math.round(dev * 10) / 10.0);
            }
            matrix.add(row);
        }

        return orderedMap("stations", stations, "hours", hours, "matrix", matrix);
    }

    private static Map<String, Object> orderedMap(Object... entries) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            result.put((String) entries[i], entries[i + 1]);
        }
        return result;
    }

    private record StationRef(String id, String name, double capacityKwp) {}
}
