package cn.techstar.pvms.backend.module.strategy.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class StrategyMockService {

    private static final AtomicInteger ID_SEQ = new AtomicInteger(100);

    private static final List<Map<String, Object>> TYPE_OPTIONS = List.of(
        option("需求响应", "demand-response"),
        option("频率调节", "frequency-regulation"),
        option("电网约束", "grid-constraint"),
        option("削峰填谷", "peak-shaving")
    );

    private static final List<Map<String, Object>> STATUS_OPTIONS = List.of(
        option("草稿", "draft"),
        option("待执行", "pending"),
        option("执行中", "executing"),
        option("已完成", "completed"),
        option("已终止", "terminated")
    );

    private static final Map<String, String> TYPE_LABEL_MAP = Map.of(
        "demand-response", "需求响应",
        "frequency-regulation", "频率调节",
        "grid-constraint", "电网约束",
        "peak-shaving", "削峰填谷"
    );

    private static final Map<String, String> STATUS_LABEL_MAP = Map.of(
        "draft", "草稿",
        "pending", "待执行",
        "executing", "执行中",
        "completed", "已完成",
        "terminated", "已终止"
    );

    private static final List<CompanyRecord> COMPANIES = List.of(
        new CompanyRecord("COM-SZ", "深圳科技有限公司"),
        new CompanyRecord("COM-WH", "武汉绿能有限公司"),
        new CompanyRecord("COM-HF", "合肥研发集团"),
        new CompanyRecord("COM-TJ", "天津港能源有限公司"),
        new CompanyRecord("COM-CD", "成都西部能源有限公司")
    );

    private static final List<StationRef> STATION_REFS = List.of(
        new StationRef("ST-001", "深圳港科园区光伏电站", "COM-SZ", 2500, 800),
        new StationRef("ST-002", "松山湖智造园光伏电站", "COM-SZ", 6200, 2100),
        new StationRef("ST-003", "南山科技园分布式", "COM-SZ", 1800, 600),
        new StationRef("ST-004", "前海综合能源站", "COM-SZ", 3200, 1050),
        new StationRef("ST-005", "坪山新能源基地", "COM-SZ", 4500, 1500),
        new StationRef("ST-006", "光明产业园光伏", "COM-SZ", 2800, 920),
        new StationRef("ST-007", "武汉物流基地光伏电站", "COM-WH", 4800, 1600),
        new StationRef("ST-008", "武汉光谷园区光伏", "COM-WH", 3600, 1200),
        new StationRef("ST-009", "黄陂仓储光伏", "COM-WH", 2200, 720),
        new StationRef("ST-010", "合肥研发中心光伏电站", "COM-HF", 1800, 600),
        new StationRef("ST-011", "肥西产业园光伏", "COM-HF", 3000, 1000),
        new StationRef("ST-012", "天津港储能园光伏电站", "COM-TJ", 3200, 1050),
        new StationRef("ST-013", "滨海新区光伏", "COM-TJ", 2600, 860),
        new StationRef("ST-014", "成都西部基地光伏电站", "COM-CD", 5400, 1800),
        new StationRef("ST-015", "双流工业园光伏", "COM-CD", 2400, 800),
        new StationRef("ST-016", "新都物流园光伏", "COM-CD", 1600, 520)
    );

    private static final List<Map<String, Object>> PRICE_PERIODS = List.of(
        pricePeriod("00:00", "06:00", "valley", "谷", 0.25, "填谷"),
        pricePeriod("06:00", "08:00", "flat", "平", 0.65, "正常运行"),
        pricePeriod("08:00", "11:00", "peak", "峰", 1.11, "削峰"),
        pricePeriod("11:00", "13:00", "peak", "峰", 1.11, "削峰"),
        pricePeriod("13:00", "15:00", "flat", "平", 0.65, "正常运行"),
        pricePeriod("15:00", "17:00", "flat", "平", 0.65, "正常运行"),
        pricePeriod("17:00", "19:00", "peak", "峰", 1.11, "削峰"),
        pricePeriod("19:00", "22:00", "flat", "平", 0.65, "正常运行"),
        pricePeriod("22:00", "24:00", "valley", "谷", 0.25, "填谷")
    );

    private final List<StrategyRecord> strategies = new ArrayList<>(buildInitialStrategies());

    public Map<String, Object> getMeta() {
        return orderedMap(
            "types", TYPE_OPTIONS,
            "statuses", STATUS_OPTIONS,
            "stations", STATION_REFS.stream().map(s -> orderedMap("id", s.id(), "name", s.name())).toList(),
            "companies", COMPANIES.stream().map(c -> orderedMap("id", c.id(), "name", c.name())).toList(),
            "pricePeriods", PRICE_PERIODS
        );
    }

    public Map<String, Object> getTree() {
        List<Map<String, Object>> items = COMPANIES.stream().map(company -> {
            List<Map<String, Object>> children = STATION_REFS.stream()
                .filter(s -> Objects.equals(s.companyId(), company.id()))
                .map(s -> orderedMap(
                    "id", s.id(), "name", s.name(), "type", "station",
                    "capacityKwp", s.capacityKwp(), "loadBaseKw", s.loadBaseKw()
                )).toList();
            return orderedMap("id", company.id(), "name", company.name(), "type", "company", "children", children);
        }).toList();
        return orderedMap("items", items);
    }

    public Map<String, Object> getKpi() {
        long activeCount = strategies.stream().filter(s -> "executing".equals(s.status) || "pending".equals(s.status)).count();
        double todayRevenue = strategies.stream()
            .filter(s -> "executing".equals(s.status) || "completed".equals(s.status))
            .mapToDouble(s -> s.estimatedRevenue).sum();
        long completedCount = strategies.stream().filter(s -> "completed".equals(s.status)).count();
        long executedTotal = completedCount + strategies.stream().filter(s -> "terminated".equals(s.status)).count();
        double successRate = executedTotal > 0 ? Math.round(completedCount * 1000.0 / executedTotal) / 10.0 : 100.0;
        long pendingCount = strategies.stream().filter(s -> "draft".equals(s.status)).count();

        return orderedMap(
            "activeCount", activeCount,
            "todayRevenue", Math.round(todayRevenue),
            "successRate", successRate,
            "pendingCount", pendingCount
        );
    }

    public Map<String, Object> getList(String status, String type, String stationId) {
        List<StrategyRecord> filtered = strategies.stream()
            .filter(s -> status == null || status.isBlank() || Objects.equals(s.status, status))
            .filter(s -> type == null || type.isBlank() || Objects.equals(s.type, type))
            .filter(s -> stationId == null || stationId.isBlank() || Objects.equals(s.stationId, stationId))
            .toList();

        return orderedMap("items", filtered.stream().map(this::mapStrategy).toList());
    }

    public Map<String, Object> getDetail(String id) {
        StrategyRecord record = strategies.stream()
            .filter(s -> Objects.equals(s.id, id))
            .findFirst().orElse(strategies.getFirst());

        Map<String, Object> base = mapStrategy(record);
        base.put("periods", record.periods);
        base.put("powerUpperLimitKw", record.powerUpperLimitKw);
        base.put("powerLowerLimitKw", record.powerLowerLimitKw);
        base.put("actualRevenue", record.actualRevenue);
        base.put("pricePeriods", PRICE_PERIODS);
        base.put("remark", record.remark);
        base.put("version", record.version);
        return base;
    }

    public Map<String, Object> getElectricityPrice(String stationId, String date) {
        return orderedMap("periods", PRICE_PERIODS);
    }

    public Map<String, Object> createStrategy(Map<String, Object> data) {
        String id = "STR-NEW-" + ID_SEQ.incrementAndGet();
        StrategyRecord record = new StrategyRecord();
        record.id = id;
        record.name = (String) data.getOrDefault("name", "新策略");
        record.stationId = (String) data.getOrDefault("stationId", "ST-001");
        record.type = (String) data.getOrDefault("type", "peak-shaving");
        record.status = "draft";
        record.executeDate = (String) data.getOrDefault("executeDate", LocalDate.now().toString());
        record.periods = data.containsKey("periods") ? (List<?>) data.get("periods") : List.of();
        record.targetPowerKw = ((Number) data.getOrDefault("targetPowerKw", 1000)).doubleValue();
        record.powerUpperLimitKw = ((Number) data.getOrDefault("powerUpperLimitKw", 1200)).doubleValue();
        record.powerLowerLimitKw = ((Number) data.getOrDefault("powerLowerLimitKw", 800)).doubleValue();
        record.estimatedRevenue = Math.round(record.targetPowerKw * 1.2);
        record.actualRevenue = 0;
        record.remark = (String) data.getOrDefault("remark", "");
        record.version = 1;
        strategies.add(record);
        return orderedMap("id", id);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> batchCreate(Map<String, Object> data) {
        List<Map<String, Object>> items = (List<Map<String, Object>>) data.getOrDefault("items", List.of());
        List<String> ids = new ArrayList<>();
        for (Map<String, Object> item : items) {
            Map<String, Object> result = createStrategy(item);
            ids.add((String) result.get("id"));
        }
        return orderedMap("ids", ids);
    }

    public Map<String, Object> submit(String id) {
        strategies.stream().filter(s -> Objects.equals(s.id, id)).findFirst()
            .ifPresent(s -> s.status = "pending");
        return orderedMap("success", true);
    }

    public Map<String, Object> terminate(String id) {
        strategies.stream().filter(s -> Objects.equals(s.id, id)).findFirst()
            .ifPresent(s -> s.status = "terminated");
        return orderedMap("success", true);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> batchSubmit(Map<String, Object> data) {
        List<String> ids = (List<String>) data.getOrDefault("ids", List.of());
        int count = 0;
        for (String id : ids) {
            var opt = strategies.stream().filter(s -> Objects.equals(s.id, id)).findFirst();
            if (opt.isPresent() && ("draft".equals(opt.get().status) || "pending".equals(opt.get().status))) {
                opt.get().status = "pending";
                count++;
            }
        }
        return orderedMap("success", true, "count", count);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> batchDelete(Map<String, Object> data) {
        List<String> ids = (List<String>) data.getOrDefault("ids", List.of());
        int count = (int) strategies.stream()
            .filter(s -> ids.contains(s.id) && "draft".equals(s.status)).count();
        strategies.removeIf(s -> ids.contains(s.id) && "draft".equals(s.status));
        return orderedMap("success", true, "count", count);
    }

    public Map<String, Object> simulate(Map<String, Object> data) {
        double targetPower = ((Number) data.getOrDefault("targetPowerKw", 1000)).doubleValue();
        double estimated = Math.round(targetPower * 1.2);
        double min = Math.round(estimated * 0.85);
        double max = Math.round(estimated * 1.15);

        List<Map<String, Object>> breakdown = List.of(
            orderedMap("period", "08:00-11:00", "revenue", Math.round(estimated * 0.35), "priceType", "peak"),
            orderedMap("period", "11:00-13:00", "revenue", Math.round(estimated * 0.25), "priceType", "peak"),
            orderedMap("period", "13:00-17:00", "revenue", Math.round(estimated * 0.2), "priceType", "flat"),
            orderedMap("period", "17:00-19:00", "revenue", Math.round(estimated * 0.2), "priceType", "peak")
        );

        return orderedMap(
            "estimatedRevenue", estimated,
            "confidence", 0.85,
            "revenueRange", orderedMap("min", min, "max", max),
            "breakdown", breakdown
        );
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> batchSimulate(Map<String, Object> data) {
        List<Map<String, Object>> items = (List<Map<String, Object>>) data.getOrDefault("items", List.of());
        double totalRevenue = 0;
        List<Map<String, Object>> results = new ArrayList<>();
        for (Map<String, Object> item : items) {
            double targetPower = ((Number) item.getOrDefault("targetPowerKw", 1000)).doubleValue();
            double estimated = Math.round(targetPower * 1.2);
            totalRevenue += estimated;
            String sid = (String) item.getOrDefault("stationId", "ST-001");
            String stationName = STATION_REFS.stream()
                .filter(s -> Objects.equals(s.id(), sid)).findFirst()
                .map(StationRef::name).orElse("未知电站");
            results.add(orderedMap("stationId", sid, "stationName", stationName, "estimatedRevenue", estimated));
        }
        return orderedMap("totalEstimatedRevenue", Math.round(totalRevenue), "items", results);
    }

    public Map<String, Object> getRevenueSummary() {
        List<StrategyRecord> revenueRecords = strategies.stream()
            .filter(s -> "completed".equals(s.status) || "executing".equals(s.status)).toList();

        double monthlyTotal = revenueRecords.stream().mapToDouble(s -> s.actualRevenue > 0 ? s.actualRevenue : s.estimatedRevenue).sum();
        double dailyAvg = Math.round(monthlyTotal / 30);
        long completed = strategies.stream().filter(s -> "completed".equals(s.status)).count();
        long total = completed + strategies.stream().filter(s -> "terminated".equals(s.status)).count();
        double successRate = total > 0 ? Math.round(completed * 1000.0 / total) / 10.0 : 100.0;

        List<String> dates = new ArrayList<>();
        List<Double> dailyRevenue = new ArrayList<>();
        List<Double> cumulativeRevenue = new ArrayList<>();
        double cumulative = 0;
        for (int i = 0; i < 28; i++) {
            dates.add(LocalDate.of(2026, 3, 1 + i).toString());
            double daily = Math.round(dailyAvg * (0.7 + (i % 7) * 0.08));
            dailyRevenue.add(daily);
            cumulative += daily;
            cumulativeRevenue.add(cumulative);
        }

        Map<String, Double> byType = new LinkedHashMap<>();
        for (StrategyRecord s : revenueRecords) {
            String label = TYPE_LABEL_MAP.getOrDefault(s.type, s.type);
            byType.merge(label, s.actualRevenue > 0 ? s.actualRevenue : s.estimatedRevenue, Double::sum);
        }
        List<Map<String, Object>> typeBreakdown = byType.entrySet().stream()
            .map(e -> orderedMap("type", e.getKey(), "revenue", Math.round(e.getValue())))
            .toList();

        return orderedMap(
            "kpi", orderedMap(
                "monthlyTotal", Math.round(monthlyTotal),
                "dailyAvg", dailyAvg,
                "successRate", successRate,
                "monthOverMonth", 12.3
            ),
            "trend", orderedMap("dates", dates, "dailyRevenue", dailyRevenue, "cumulativeRevenue", cumulativeRevenue),
            "typeBreakdown", typeBreakdown
        );
    }

    public Map<String, Object> getRevenueDetail() {
        List<StrategyRecord> revenueRecords = strategies.stream()
            .filter(s -> "completed".equals(s.status) || "executing".equals(s.status)).toList();

        return orderedMap("items", revenueRecords.stream().map(s -> {
            String stationName = STATION_REFS.stream()
                .filter(sr -> Objects.equals(sr.id(), s.stationId))
                .findFirst().map(StationRef::name).orElse("未知电站");
            double actual = s.actualRevenue > 0 ? s.actualRevenue : s.estimatedRevenue * 0.96;
            double deviation = s.estimatedRevenue > 0 ? Math.round((actual - s.estimatedRevenue) * 1000.0 / s.estimatedRevenue) / 10.0 : 0;
            String timePeriod = s.periods.isEmpty() ? "" :
                s.periods.getFirst().toString().replaceAll("[{} ]", "");

            return orderedMap(
                "id", s.id, "name", s.name, "stationName", stationName,
                "typeLabel", TYPE_LABEL_MAP.getOrDefault(s.type, s.type),
                "timePeriod", formatPeriods(s.periods),
                "estimatedRevenue", s.estimatedRevenue,
                "actualRevenue", Math.round(actual),
                "deviationRate", deviation
            );
        }).toList());
    }

    public Map<String, Object> getCompare(String ids) {
        String[] idArr = ids.split(",");
        List<Map<String, Object>> items = new ArrayList<>();
        for (String id : idArr) {
            String trimmedId = id.trim();
            strategies.stream().filter(s -> Objects.equals(s.id, trimmedId)).findFirst().ifPresent(s -> {
                String stationName = STATION_REFS.stream()
                    .filter(sr -> Objects.equals(sr.id(), s.stationId))
                    .findFirst().map(StationRef::name).orElse("未知电站");
                double actual = s.actualRevenue > 0 ? s.actualRevenue : s.estimatedRevenue * 0.96;
                double deviation = s.estimatedRevenue > 0 ? Math.round((actual - s.estimatedRevenue) * 1000.0 / s.estimatedRevenue) / 10.0 : 0;
                items.add(orderedMap(
                    "id", s.id, "name", s.name, "stationName", stationName,
                    "typeLabel", TYPE_LABEL_MAP.getOrDefault(s.type, s.type),
                    "targetPowerKw", s.targetPowerKw,
                    "estimatedRevenue", s.estimatedRevenue,
                    "actualRevenue", Math.round(actual),
                    "deviationRate", deviation
                ));
            });
        }
        return orderedMap("items", items);
    }

    private Map<String, Object> mapStrategy(StrategyRecord s) {
        String stationName = STATION_REFS.stream()
            .filter(sr -> Objects.equals(sr.id(), s.stationId))
            .findFirst().map(StationRef::name).orElse("未知电站");
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("id", s.id);
        result.put("name", s.name);
        result.put("stationId", s.stationId);
        result.put("stationName", stationName);
        result.put("type", s.type);
        result.put("typeLabel", TYPE_LABEL_MAP.getOrDefault(s.type, s.type));
        result.put("status", s.status);
        result.put("statusLabel", STATUS_LABEL_MAP.getOrDefault(s.status, s.status));
        result.put("executeDate", s.executeDate);
        result.put("timePeriod", formatPeriods(s.periods));
        result.put("targetPowerKw", s.targetPowerKw);
        result.put("estimatedRevenue", s.estimatedRevenue);
        return result;
    }

    private String formatPeriods(List<?> periods) {
        if (periods == null || periods.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Object p : periods) {
            if (!sb.isEmpty()) sb.append(", ");
            if (p instanceof Map<?, ?> map) {
                Object start = map.get("start");
                Object end = map.get("end");
                sb.append(start != null ? start : "").append("-").append(end != null ? end : "");
            } else {
                sb.append(p);
            }
        }
        return sb.toString();
    }

    private static List<StrategyRecord> buildInitialStrategies() {
        String[] types = {"demand-response", "frequency-regulation", "grid-constraint", "peak-shaving"};
        String[] statuses = {"executing", "executing", "executing", "executing", "executing",
            "pending", "pending", "pending", "pending", "pending",
            "completed", "completed", "completed", "completed", "completed", "completed", "completed", "completed",
            "terminated", "terminated",
            "draft", "draft", "draft", "draft", "draft"};

        List<StrategyRecord> result = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            StrategyRecord s = new StrategyRecord();
            s.id = String.format("STR-%03d", i + 1);
            StationRef station = STATION_REFS.get(i % STATION_REFS.size());
            s.stationId = station.id();
            s.type = types[i % 4];
            s.status = statuses[i];
            s.name = TYPE_LABEL_MAP.get(s.type) + "策略-" + station.name().substring(0, Math.min(4, station.name().length()));
            s.executeDate = switch (s.status) {
                case "executing" -> "2026-03-29";
                case "pending" -> "2026-03-30";
                case "completed" -> "2026-03-28";
                case "terminated" -> "2026-03-27";
                default -> "2026-03-31";
            };
            s.targetPowerKw = Math.round(station.capacityKwp() * 0.72);
            s.powerUpperLimitKw = Math.round(station.capacityKwp() * 0.88);
            s.powerLowerLimitKw = Math.round(station.capacityKwp() * 0.56);
            s.estimatedRevenue = Math.round(s.targetPowerKw * 1.2);
            s.actualRevenue = ("completed".equals(s.status) || "executing".equals(s.status))
                ? Math.round(s.estimatedRevenue * (0.92 + Math.random() * 0.16)) : 0;
            s.periods = List.of(orderedMap("start", "08:00", "end", "11:00"), orderedMap("start", "17:00", "end", "19:00"));
            s.remark = "";
            s.version = 1;
            result.add(s);
        }
        return result;
    }

    private static Map<String, Object> option(String label, String value) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("label", label);
        result.put("value", value);
        return result;
    }

    private static Map<String, Object> pricePeriod(String start, String end, String type, String typeLabel, double price, String action) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("start", start);
        result.put("end", end);
        result.put("type", type);
        result.put("typeLabel", typeLabel);
        result.put("price", price);
        result.put("action", action);
        return result;
    }

    private static Map<String, Object> orderedMap(Object... entries) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            result.put((String) entries[i], entries[i + 1]);
        }
        return result;
    }

    private record CompanyRecord(String id, String name) {}
    private record StationRef(String id, String name, String companyId, double capacityKwp, double loadBaseKw) {}

    private static class StrategyRecord {
        String id;
        String name;
        String stationId;
        String type;
        String status;
        String executeDate;
        List<?> periods;
        double targetPowerKw;
        double powerUpperLimitKw;
        double powerLowerLimitKw;
        double estimatedRevenue;
        double actualRevenue;
        String remark;
        int version;
    }
}
