package cn.techstar.pvms.backend.module.strategy.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StrategySupport {

    public static final LocalDate DEFAULT_BIZ_DATE = LocalDate.of(2026, 3, 30);
    public static final List<String> TYPE_ORDER = List.of(
        "peak-shaving",
        "demand-response",
        "grid-constraint",
        "frequency-regulation"
    );
    public static final Map<String, String> TYPE_LABELS = Map.of(
        "peak-shaving", "Peak Shaving",
        "demand-response", "Demand Response",
        "grid-constraint", "Grid Constraint",
        "frequency-regulation", "Frequency Regulation"
    );
    public static final List<String> STATUS_ORDER = List.of(
        "draft",
        "pending",
        "executing",
        "completed",
        "terminated"
    );
    public static final Map<String, StatusMeta> STATUS_META = Map.of(
        "draft", new StatusMeta("Draft", "info"),
        "pending", new StatusMeta("Pending", "warning"),
        "executing", new StatusMeta("Executing", "success"),
        "completed", new StatusMeta("Completed", "success"),
        "terminated", new StatusMeta("Terminated", "danger")
    );
    public static final Map<String, Double> TYPE_PRICE_FACTOR = Map.of(
        "peak-shaving", 1.00,
        "demand-response", 0.88,
        "grid-constraint", 0.82,
        "frequency-regulation", 0.93
    );
    public static final Map<String, Double> TYPE_REWARD_FACTOR = Map.of(
        "peak-shaving", 0.16,
        "demand-response", 0.22,
        "grid-constraint", 0.14,
        "frequency-regulation", 0.19
    );
    public static final Map<String, Double> TYPE_PENALTY_FACTOR = Map.of(
        "peak-shaving", 0.09,
        "demand-response", 0.12,
        "grid-constraint", 0.11,
        "frequency-regulation", 0.10
    );

    private StrategySupport() {
    }

    public static String typeLabel(String type) {
        return TYPE_LABELS.getOrDefault(type, type);
    }

    public static StatusMeta statusMeta(String status) {
        return STATUS_META.getOrDefault(status, new StatusMeta(status, "info"));
    }

    public static int slotOf(LocalDateTime time) {
        if (time == null) {
            return 0;
        }
        int slot = time.getHour() * 4 + time.getMinute() / 15;
        return Math.max(0, Math.min(slot, 95));
    }

    public static int endSlotExclusive(LocalDateTime time) {
        if (time == null) {
            return 96;
        }
        int slot = time.getHour() * 4 + time.getMinute() / 15;
        return Math.max(slot + (time.getMinute() % 15 == 0 ? 0 : 1), StrategySupport.slotOf(time) + 1);
    }

    public static double round(double value, int scale) {
        double factor = Math.pow(10, scale);
        return Math.round(value * factor) / factor;
    }

    public static Map<String, Object> orderedMap(Object... entries) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            map.put((String) entries[index], entries[index + 1]);
        }
        return map;
    }

    public record StatusMeta(String label, String tagType) {
    }
}
