package cn.techstar.pvms.backend.module.strategy.service;

import cn.techstar.pvms.backend.module.strategy.model.StrategyRequest;
import cn.techstar.pvms.backend.module.strategy.repository.StrategyMetaMapper;
import cn.techstar.pvms.backend.module.strategy.repository.StrategyPriceMapper;
import cn.techstar.pvms.backend.module.strategy.repository.StrategyRecordMapper;
import cn.techstar.pvms.backend.module.strategy.repository.StrategyRevenueMapper;
import cn.techstar.pvms.backend.module.strategy.repository.StrategyTreeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class StrategyDataService {

    private final StrategyMetaMapper metaMapper;
    private final StrategyTreeMapper treeMapper;
    private final StrategyRecordMapper recordMapper;
    private final StrategyPriceMapper priceMapper;
    private final StrategyRevenueMapper revenueMapper;
    private final StrategySimulationService simulationService;

    public StrategyDataService(
        StrategyMetaMapper metaMapper,
        StrategyTreeMapper treeMapper,
        StrategyRecordMapper recordMapper,
        StrategyPriceMapper priceMapper,
        StrategyRevenueMapper revenueMapper,
        StrategySimulationService simulationService
    ) {
        this.metaMapper = metaMapper;
        this.treeMapper = treeMapper;
        this.recordMapper = recordMapper;
        this.priceMapper = priceMapper;
        this.revenueMapper = revenueMapper;
        this.simulationService = simulationService;
    }

    public Map<String, Object> getMeta() {
        List<StrategyMetaMapper.CompanyRow> companies = metaMapper.findCompanies();
        List<StrategyMetaMapper.StationRow> stations = metaMapper.findStations();

        return StrategySupport.orderedMap(
            "defaultStationId", stations.isEmpty() ? "" : stations.getFirst().id(),
            "defaultType", StrategySupport.TYPE_ORDER.getFirst(),
            "companies", companies.stream()
                .map(company -> StrategySupport.orderedMap(
                    "id", company.id(),
                    "name", company.name(),
                    "region", company.region()
                ))
                .toList(),
            "stations", stations.stream()
                .map(station -> StrategySupport.orderedMap(
                    "id", station.id(),
                    "companyId", station.companyId(),
                    "companyName", station.companyName(),
                    "region", station.region(),
                    "name", station.name(),
                    "capacityKwp", StrategySupport.round(station.capacityKwp(), 2),
                    "status", station.status(),
                    "dataQuality", station.dataQuality()
                ))
                .toList(),
            "types", StrategySupport.TYPE_ORDER.stream()
                .map(type -> StrategySupport.orderedMap("value", type, "label", StrategySupport.typeLabel(type)))
                .toList(),
            "statuses", StrategySupport.STATUS_ORDER.stream()
                .map(status -> {
                    StrategySupport.StatusMeta meta = StrategySupport.statusMeta(status);
                    return StrategySupport.orderedMap(
                        "value", status,
                        "label", meta.label(),
                        "tagType", meta.tagType()
                    );
                })
                .toList(),
            "pricePeriods", priceMapper.findTemplate().stream().map(this::mapPricePeriod).toList()
        );
    }

    public Map<String, Object> getTree() {
        LinkedHashMap<String, Map<String, Object>> companies = new LinkedHashMap<>();
        treeMapper.findCompanyStationRows().forEach(row -> {
            Map<String, Object> companyNode = companies.computeIfAbsent(row.companyId(), key -> {
                Map<String, Object> node = new LinkedHashMap<>();
                node.put("id", row.companyId());
                node.put("label", row.companyName());
                node.put("type", "company");
                node.put("region", row.region());
                node.put("strategyCount", 0);
                node.put("activeCount", 0);
                node.put("pendingCount", 0);
                node.put("children", new ArrayList<Map<String, Object>>());
                return node;
            });

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> children = (List<Map<String, Object>>) companyNode.get("children");
            children.add(StrategySupport.orderedMap(
                "id", row.stationId(),
                "label", row.stationName(),
                "type", "station",
                "stationId", row.stationId(),
                "capacityKwp", StrategySupport.round(row.capacityKwp(), 2),
                "status", row.stationStatus(),
                "dataQuality", row.dataQuality(),
                "strategyCount", row.strategyCount(),
                "activeCount", row.activeCount(),
                "pendingCount", row.pendingCount()
            ));

            companyNode.put("strategyCount", ((Number) companyNode.get("strategyCount")).intValue() + row.strategyCount());
            companyNode.put("activeCount", ((Number) companyNode.get("activeCount")).intValue() + row.activeCount());
            companyNode.put("pendingCount", ((Number) companyNode.get("pendingCount")).intValue() + row.pendingCount());
        });

        return StrategySupport.orderedMap("tree", new ArrayList<>(companies.values()));
    }

    public Map<String, Object> getKpi() {
        List<StrategyRecordMapper.StrategyRow> strategies = recordMapper.findAll(StrategySupport.DEFAULT_BIZ_DATE);
        List<StrategyRevenueMapper.RevenueDailyRow> todayRevenue = revenueMapper.findByDateRange(
            StrategySupport.DEFAULT_BIZ_DATE,
            StrategySupport.DEFAULT_BIZ_DATE
        );
        List<StrategyRecordMapper.ExecutionLogRow> logs = recordMapper.findAllExecutionLogs();
        long successLogs = logs.stream().filter(item -> Objects.equals(item.result(), "success")).count();

        double todayRevenueValue = todayRevenue.stream().mapToDouble(StrategyRevenueMapper.RevenueDailyRow::actualRevenueCny).sum();
        double todayEstimated = todayRevenue.stream().mapToDouble(StrategyRevenueMapper.RevenueDailyRow::estimatedRevenueCny).sum();

        return StrategySupport.orderedMap(
            "strategyCount", strategies.size(),
            "activeCount", strategies.stream().filter(item -> Objects.equals(item.status(), "executing")).count(),
            "pendingCount", strategies.stream().filter(item -> Objects.equals(item.status(), "pending")).count(),
            "draftCount", strategies.stream().filter(item -> Objects.equals(item.status(), "draft")).count(),
            "todayRevenue", StrategySupport.round(todayRevenueValue, 2),
            "successRate", logs.isEmpty() ? 0 : StrategySupport.round(successLogs * 100.0 / logs.size(), 1),
            "achievementRate", todayEstimated == 0 ? 0 : StrategySupport.round(todayRevenueValue * 100.0 / todayEstimated, 1)
        );
    }

    public Map<String, Object> getList(String status, String type, String region, String stationId, String keyword) {
        List<StrategyRecordMapper.StrategyRow> items = filterStrategies(recordMapper.findAll(StrategySupport.DEFAULT_BIZ_DATE), status, type, region, stationId, keyword);
        return StrategySupport.orderedMap(
            "items", items.stream().map(this::mapListItem).toList(),
            "total", items.size()
        );
    }

    public Map<String, Object> getDetail(String id) {
        StrategyRecordMapper.StrategyRow row = resolveStrategy(id);
        return StrategySupport.orderedMap(
            "id", row.id(),
            "name", row.name(),
            "type", row.type(),
            "typeLabel", StrategySupport.typeLabel(row.type()),
            "status", row.status(),
            "statusLabel", StrategySupport.statusMeta(row.status()).label(),
            "statusType", StrategySupport.statusMeta(row.status()).tagType(),
            "mode", row.mode(),
            "stationId", row.stationId(),
            "stationName", row.stationName(),
            "companyId", row.companyId(),
            "companyName", row.companyName(),
            "region", row.region(),
            "targetPowerKw", StrategySupport.round(row.targetPowerKw(), 2),
            "startTime", row.startTime(),
            "endTime", row.endTime(),
            "versionNo", row.versionNo(),
            "remark", row.remark(),
            "capacityKwp", StrategySupport.round(row.capacityKwp(), 2),
            "todayRevenue", StrategySupport.round(valueOrZero(row.todayActualRevenueCny()), 2),
            "estimatedRevenue", StrategySupport.round(valueOrZero(row.lastSimulatedRevenueCny()), 2),
            "confidenceRange", StrategySupport.orderedMap(
                "low", StrategySupport.round(valueOrZero(row.confidenceLowCny()), 2),
                "high", StrategySupport.round(valueOrZero(row.confidenceHighCny()), 2)
            ),
            "successProbability", StrategySupport.round(valueOrZero(row.successProbabilityPct()), 1),
            "periods", recordMapper.findPeriodsByStrategyId(row.id()).stream().map(this::mapPeriod).toList(),
            "pricePeriods", priceMapper.findByStationId(row.stationId()).stream().map(this::mapPricePeriod).toList(),
            "executionLogs", recordMapper.findExecutionLogsByStrategyId(row.id()).stream().map(this::mapExecutionLog).toList()
        );
    }

    public Map<String, Object> getElectricityPrice(String stationId) {
        StrategyMetaMapper.StationRow station = metaMapper.findStations().stream()
            .filter(item -> Objects.equals(item.id(), stationId))
            .findFirst()
            .orElse(metaMapper.findStations().getFirst());

        return StrategySupport.orderedMap(
            "stationId", station.id(),
            "stationName", station.name(),
            "periods", priceMapper.findByStationId(station.id()).stream().map(this::mapPricePeriod).toList()
        );
    }

    @Transactional
    public Map<String, Object> createStrategy(StrategyRequest request) {
        StrategyRequest normalized = normalizeRequest(request);
        String strategyId = nextStrategyId();
        StrategyMetaMapper.StationRow station = resolveStation(normalized.stationId());
        LocalDateTime now = LocalDateTime.of(2026, 3, 30, 14, 30);
        StrategySimulationService.SimulationResult simulationResult = simulationService.simulate(normalized);

        recordMapper.insertStrategy(new StrategyRecordMapper.StrategyMutationRow(
            strategyId,
            station.id(),
            station.companyId(),
            resolveName(normalized),
            normalized.type(),
            "draft",
            normalized.mode() == null || normalized.mode().isBlank() ? "single" : normalized.mode(),
            normalized.targetPowerKw(),
            normalized.startTime(),
            normalized.endTime(),
            1,
            normalized.remark(),
            now,
            now
        ));
        buildPeriods(strategyId, normalized).forEach(recordMapper::insertPeriod);
        persistSimulationArtifacts(strategyId, simulationResult);
        recordMapper.insertExecutionLog(new StrategyRecordMapper.ExecutionLogMutationRow(
            strategyId + "-LOG-" + System.currentTimeMillis(),
            strategyId,
            now,
            "created",
            "success",
            StrategySupport.round(Math.max(0, 100 - simulationResult.successProbability()) / 5.0, 1),
            "dev-h2"
        ));

        return mapListItem(resolveStrategy(strategyId));
    }

    @Transactional
    public Map<String, Object> batchCreateStrategies(List<StrategyRequest> requests) {
        List<Map<String, Object>> items = requests.stream().map(this::createStrategy).toList();
        return StrategySupport.orderedMap(
            "createdCount", items.size(),
            "items", items
        );
    }

    @Transactional
    public Map<String, Object> submitStrategy(String id) {
        return updateStatus(id, "pending", "submitted", "success");
    }

    @Transactional
    public Map<String, Object> terminateStrategy(String id) {
        return updateStatus(id, "terminated", "terminated", "success");
    }

    @Transactional
    public Map<String, Object> batchSubmit(List<String> ids) {
        List<Map<String, Object>> items = sanitizeIds(ids).stream().map(this::submitStrategy).toList();
        return StrategySupport.orderedMap(
            "updatedCount", items.size(),
            "items", items
        );
    }

    @Transactional
    public Map<String, Object> batchDelete(List<String> ids) {
        int deletedCount = 0;
        for (String id : sanitizeIds(ids)) {
            recordMapper.deleteSnapshot(id);
            recordMapper.deleteRevenueDaily(id);
            recordMapper.deleteExecutionLogs(id);
            recordMapper.deletePeriods(id);
            deletedCount += recordMapper.deleteStrategy(id);
        }
        return StrategySupport.orderedMap("deletedCount", deletedCount);
    }

    private Map<String, Object> updateStatus(String id, String nextStatus, String action, String result) {
        StrategyRecordMapper.StrategyRow row = resolveStrategy(id);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 3, 30, 14, 35);
        recordMapper.updateStatus(row.id(), nextStatus, updatedAt);
        recordMapper.insertExecutionLog(new StrategyRecordMapper.ExecutionLogMutationRow(
            row.id() + "-LOG-" + System.nanoTime(),
            row.id(),
            updatedAt,
            action,
            result,
            2.4,
            "dev-h2"
        ));
        return mapListItem(resolveStrategy(row.id()));
    }

    private List<StrategyRecordMapper.StrategyRow> filterStrategies(
        List<StrategyRecordMapper.StrategyRow> items,
        String status,
        String type,
        String region,
        String stationId,
        String keyword
    ) {
        return items.stream()
            .filter(item -> status == null || status.isBlank() || Objects.equals(item.status(), status))
            .filter(item -> type == null || type.isBlank() || Objects.equals(item.type(), type))
            .filter(item -> region == null || region.isBlank() || Objects.equals(item.region(), region))
            .filter(item -> stationId == null || stationId.isBlank() || Objects.equals(item.stationId(), stationId))
            .filter(item -> keyword == null || keyword.isBlank()
                || item.name().toLowerCase().contains(keyword.toLowerCase())
                || item.stationName().toLowerCase().contains(keyword.toLowerCase()))
            .sorted(Comparator.comparing(StrategyRecordMapper.StrategyRow::updatedAt).reversed())
            .toList();
    }

    private StrategyRecordMapper.StrategyRow resolveStrategy(String id) {
        StrategyRecordMapper.StrategyRow row = recordMapper.findById(id, StrategySupport.DEFAULT_BIZ_DATE);
        if (row != null) {
            return row;
        }
        return recordMapper.findAll(StrategySupport.DEFAULT_BIZ_DATE).stream().findFirst().orElseThrow();
    }

    private StrategyMetaMapper.StationRow resolveStation(String stationId) {
        return metaMapper.findStations().stream()
            .filter(item -> Objects.equals(item.id(), stationId))
            .findFirst()
            .orElseThrow();
    }

    private String nextStrategyId() {
        int next = recordMapper.findAll(StrategySupport.DEFAULT_BIZ_DATE).stream()
            .map(StrategyRecordMapper.StrategyRow::id)
            .map(id -> id.replace("SG-", ""))
            .mapToInt(value -> {
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException exception) {
                    return 0;
                }
            })
            .max()
            .orElse(0) + 1;
        return String.format("SG-%03d", next);
    }

    private StrategyRequest normalizeRequest(StrategyRequest request) {
        StrategyMetaMapper.StationRow fallbackStation = metaMapper.findStations().getFirst();
        String stationId = request.stationId() == null || request.stationId().isBlank() ? fallbackStation.id() : request.stationId();
        String type = request.type() == null || request.type().isBlank() ? StrategySupport.TYPE_ORDER.getFirst() : request.type();
        LocalDateTime startTime = request.startTime() == null ? StrategySupport.DEFAULT_BIZ_DATE.atTime(8, 0) : request.startTime();
        LocalDateTime endTime = request.endTime() == null ? StrategySupport.DEFAULT_BIZ_DATE.atTime(18, 0) : request.endTime();
        double targetPowerKw = request.targetPowerKw() == null ? 800.0 : request.targetPowerKw();
        return new StrategyRequest(
            stationId,
            type,
            request.name(),
            targetPowerKw,
            startTime,
            endTime,
            request.remark(),
            request.mode()
        );
    }

    private String resolveName(StrategyRequest request) {
        if (request.name() != null && !request.name().isBlank()) {
            return request.name();
        }
        return StrategySupport.typeLabel(request.type()) + " Strategy";
    }

    private List<StrategyRecordMapper.StrategyPeriodMutationRow> buildPeriods(String strategyId, StrategyRequest request) {
        int startSlot = StrategySupport.slotOf(request.startTime());
        int endSlotExclusive = Math.min(96, Math.max(startSlot + 1, StrategySupport.endSlotExclusive(request.endTime())));
        int span = Math.max(1, endSlotExclusive - startSlot);
        if (span < 8) {
            return List.of(new StrategyRecordMapper.StrategyPeriodMutationRow(
                strategyId,
                1,
                startSlot,
                endSlotExclusive - 1,
                primaryAction(request.type()),
                1.0
            ));
        }

        int firstEnd = startSlot + span / 2;
        return List.of(
            new StrategyRecordMapper.StrategyPeriodMutationRow(
                strategyId,
                1,
                startSlot,
                Math.max(startSlot, firstEnd - 1),
                primaryAction(request.type()),
                0.85
            ),
            new StrategyRecordMapper.StrategyPeriodMutationRow(
                strategyId,
                2,
                firstEnd,
                endSlotExclusive - 1,
                "stabilize",
                1.0
            )
        );
    }

    private void persistSimulationArtifacts(String strategyId, StrategySimulationService.SimulationResult simulationResult) {
        recordMapper.insertSnapshot(new StrategyRecordMapper.StrategySnapshotMutationRow(
            strategyId,
            simulationResult.estimatedRevenue(),
            simulationResult.confidenceLow(),
            simulationResult.confidenceHigh(),
            simulationResult.successProbability()
        ));
        revenueMapper.insertRevenueDaily(new StrategyRevenueMapper.RevenueDailyMutationRow(
            strategyId,
            StrategySupport.DEFAULT_BIZ_DATE,
            simulationResult.estimatedRevenue(),
            StrategySupport.round(simulationResult.estimatedRevenue() * 0.96, 2),
            simulationResult.peakSaving(),
            simulationResult.responseReward(),
            simulationResult.penalty()
        ));
    }

    private List<String> sanitizeIds(List<String> ids) {
        return ids == null ? List.of() : ids.stream()
            .filter(item -> item != null && !item.isBlank())
            .collect(Collectors.toCollection(LinkedHashSet::new))
            .stream()
            .toList();
    }

    private String primaryAction(String type) {
        return switch (type) {
            case "demand-response" -> "respond";
            case "grid-constraint" -> "limit";
            case "frequency-regulation" -> "regulate";
            default -> "shave";
        };
    }

    private Map<String, Object> mapListItem(StrategyRecordMapper.StrategyRow row) {
        StrategySupport.StatusMeta statusMeta = StrategySupport.statusMeta(row.status());
        return StrategySupport.orderedMap(
            "id", row.id(),
            "name", row.name(),
            "type", row.type(),
            "typeLabel", StrategySupport.typeLabel(row.type()),
            "status", row.status(),
            "statusLabel", statusMeta.label(),
            "statusType", statusMeta.tagType(),
            "stationId", row.stationId(),
            "stationName", row.stationName(),
            "companyId", row.companyId(),
            "companyName", row.companyName(),
            "region", row.region(),
            "mode", row.mode(),
            "targetPowerKw", StrategySupport.round(row.targetPowerKw(), 2),
            "startTime", row.startTime(),
            "endTime", row.endTime(),
            "updatedAt", row.updatedAt(),
            "todayRevenue", StrategySupport.round(valueOrZero(row.todayActualRevenueCny()), 2),
            "estimatedRevenue", StrategySupport.round(valueOrZero(row.lastSimulatedRevenueCny()), 2),
            "successProbability", StrategySupport.round(valueOrZero(row.successProbabilityPct()), 1),
            "confidenceRange", StrategySupport.orderedMap(
                "low", StrategySupport.round(valueOrZero(row.confidenceLowCny()), 2),
                "high", StrategySupport.round(valueOrZero(row.confidenceHighCny()), 2)
            )
        );
    }

    private Map<String, Object> mapPeriod(StrategyRecordMapper.StrategyPeriodRow row) {
        return StrategySupport.orderedMap(
            "periodOrder", row.periodOrder(),
            "startSlot", row.startSlot(),
            "endSlot", row.endSlot(),
            "actionType", row.actionType(),
            "targetRatio", StrategySupport.round(row.targetRatio(), 3)
        );
    }

    private Map<String, Object> mapPricePeriod(StrategyPriceMapper.PricePeriodRow row) {
        int nextSlot = Math.min(96, row.endSlot() + 1);
        return StrategySupport.orderedMap(
            "periodOrder", row.periodOrder(),
            "startSlot", row.startSlot(),
            "endSlot", row.endSlot(),
            "startLabel", String.format("%02d:%02d", row.startSlot() / 4, (row.startSlot() % 4) * 15),
            "endLabel", nextSlot >= 96 ? "24:00" : String.format("%02d:%02d", nextSlot / 4, (nextSlot % 4) * 15),
            "priceType", row.priceType(),
            "price", StrategySupport.round(row.priceCnyPerKwh(), 3)
        );
    }

    private Map<String, Object> mapExecutionLog(StrategyRecordMapper.ExecutionLogRow row) {
        return StrategySupport.orderedMap(
            "id", row.id(),
            "time", row.eventTime(),
            "action", row.action(),
            "result", row.result(),
            "deviationRate", StrategySupport.round(row.deviationRatePct(), 1),
            "operator", row.operatorName()
        );
    }

    private double valueOrZero(Double value) {
        return value == null ? 0 : value;
    }
}
