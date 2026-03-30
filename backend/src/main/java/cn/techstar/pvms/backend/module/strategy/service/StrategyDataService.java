package cn.techstar.pvms.backend.module.strategy.service;

import cn.techstar.pvms.backend.module.strategy.model.StrategyRequest;
import cn.techstar.pvms.backend.module.strategy.repository.StrategyMetaRepository;
import cn.techstar.pvms.backend.module.strategy.repository.StrategyPriceRepository;
import cn.techstar.pvms.backend.module.strategy.repository.StrategyRecordRepository;
import cn.techstar.pvms.backend.module.strategy.repository.StrategyRevenueRepository;
import cn.techstar.pvms.backend.module.strategy.repository.StrategyTreeRepository;
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

    private final StrategyMetaRepository metaRepository;
    private final StrategyTreeRepository treeRepository;
    private final StrategyRecordRepository recordRepository;
    private final StrategyPriceRepository priceRepository;
    private final StrategyRevenueRepository revenueRepository;
    private final StrategySimulationService simulationService;

    public StrategyDataService(
        StrategyMetaRepository metaRepository,
        StrategyTreeRepository treeRepository,
        StrategyRecordRepository recordRepository,
        StrategyPriceRepository priceRepository,
        StrategyRevenueRepository revenueRepository,
        StrategySimulationService simulationService
    ) {
        this.metaRepository = metaRepository;
        this.treeRepository = treeRepository;
        this.recordRepository = recordRepository;
        this.priceRepository = priceRepository;
        this.revenueRepository = revenueRepository;
        this.simulationService = simulationService;
    }

    public Map<String, Object> getMeta() {
        List<StrategyMetaRepository.CompanyRow> companies = metaRepository.findCompanies();
        List<StrategyMetaRepository.StationRow> stations = metaRepository.findStations();

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
            "pricePeriods", priceRepository.findTemplate().stream().map(this::mapPricePeriod).toList()
        );
    }

    public Map<String, Object> getTree() {
        LinkedHashMap<String, Map<String, Object>> companies = new LinkedHashMap<>();
        treeRepository.findCompanyStationRows().forEach(row -> {
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
        List<StrategyRecordRepository.StrategyRow> strategies = recordRepository.findAll(StrategySupport.DEFAULT_BIZ_DATE);
        List<StrategyRevenueRepository.RevenueDailyRow> todayRevenue = revenueRepository.findByDateRange(
            StrategySupport.DEFAULT_BIZ_DATE,
            StrategySupport.DEFAULT_BIZ_DATE
        );
        List<StrategyRecordRepository.ExecutionLogRow> logs = recordRepository.findAllExecutionLogs();
        long successLogs = logs.stream().filter(item -> Objects.equals(item.result(), "success")).count();

        double todayRevenueValue = todayRevenue.stream().mapToDouble(StrategyRevenueRepository.RevenueDailyRow::actualRevenueCny).sum();
        double todayEstimated = todayRevenue.stream().mapToDouble(StrategyRevenueRepository.RevenueDailyRow::estimatedRevenueCny).sum();

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
        List<StrategyRecordRepository.StrategyRow> items = filterStrategies(recordRepository.findAll(StrategySupport.DEFAULT_BIZ_DATE), status, type, region, stationId, keyword);
        return StrategySupport.orderedMap(
            "items", items.stream().map(this::mapListItem).toList(),
            "total", items.size()
        );
    }

    public Map<String, Object> getDetail(String id) {
        StrategyRecordRepository.StrategyRow row = resolveStrategy(id);
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
            "periods", recordRepository.findPeriodsByStrategyId(row.id()).stream().map(this::mapPeriod).toList(),
            "pricePeriods", priceRepository.findByStationId(row.stationId()).stream().map(this::mapPricePeriod).toList(),
            "executionLogs", recordRepository.findExecutionLogsByStrategyId(row.id()).stream().map(this::mapExecutionLog).toList()
        );
    }

    public Map<String, Object> getElectricityPrice(String stationId) {
        StrategyMetaRepository.StationRow station = metaRepository.findStations().stream()
            .filter(item -> Objects.equals(item.id(), stationId))
            .findFirst()
            .orElse(metaRepository.findStations().getFirst());

        return StrategySupport.orderedMap(
            "stationId", station.id(),
            "stationName", station.name(),
            "periods", priceRepository.findByStationId(station.id()).stream().map(this::mapPricePeriod).toList()
        );
    }

    @Transactional
    public Map<String, Object> createStrategy(StrategyRequest request) {
        StrategyRequest normalized = normalizeRequest(request);
        String strategyId = nextStrategyId();
        StrategyMetaRepository.StationRow station = resolveStation(normalized.stationId());
        LocalDateTime now = LocalDateTime.of(2026, 3, 30, 14, 30);
        StrategySimulationService.SimulationResult simulationResult = simulationService.simulate(normalized);

        recordRepository.insertStrategy(new StrategyRecordRepository.StrategyMutationRow(
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
        buildPeriods(strategyId, normalized).forEach(recordRepository::insertPeriod);
        persistSimulationArtifacts(strategyId, simulationResult);
        recordRepository.insertExecutionLog(new StrategyRecordRepository.ExecutionLogMutationRow(
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
            recordRepository.deleteSnapshot(id);
            recordRepository.deleteRevenueDaily(id);
            recordRepository.deleteExecutionLogs(id);
            recordRepository.deletePeriods(id);
            deletedCount += recordRepository.deleteStrategy(id);
        }
        return StrategySupport.orderedMap("deletedCount", deletedCount);
    }

    private Map<String, Object> updateStatus(String id, String nextStatus, String action, String result) {
        StrategyRecordRepository.StrategyRow row = resolveStrategy(id);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 3, 30, 14, 35);
        recordRepository.updateStatus(row.id(), nextStatus, updatedAt);
        recordRepository.insertExecutionLog(new StrategyRecordRepository.ExecutionLogMutationRow(
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

    private List<StrategyRecordRepository.StrategyRow> filterStrategies(
        List<StrategyRecordRepository.StrategyRow> items,
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
            .sorted(Comparator.comparing(StrategyRecordRepository.StrategyRow::updatedAt).reversed())
            .toList();
    }

    private StrategyRecordRepository.StrategyRow resolveStrategy(String id) {
        StrategyRecordRepository.StrategyRow row = recordRepository.findById(id, StrategySupport.DEFAULT_BIZ_DATE);
        if (row != null) {
            return row;
        }
        return recordRepository.findAll(StrategySupport.DEFAULT_BIZ_DATE).stream().findFirst().orElseThrow();
    }

    private StrategyMetaRepository.StationRow resolveStation(String stationId) {
        return metaRepository.findStations().stream()
            .filter(item -> Objects.equals(item.id(), stationId))
            .findFirst()
            .orElseThrow();
    }

    private String nextStrategyId() {
        int next = recordRepository.findAll(StrategySupport.DEFAULT_BIZ_DATE).stream()
            .map(StrategyRecordRepository.StrategyRow::id)
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
        StrategyMetaRepository.StationRow fallbackStation = metaRepository.findStations().getFirst();
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

    private List<StrategyRecordRepository.StrategyPeriodMutationRow> buildPeriods(String strategyId, StrategyRequest request) {
        int startSlot = StrategySupport.slotOf(request.startTime());
        int endSlotExclusive = Math.min(96, Math.max(startSlot + 1, StrategySupport.endSlotExclusive(request.endTime())));
        int span = Math.max(1, endSlotExclusive - startSlot);
        if (span < 8) {
            return List.of(new StrategyRecordRepository.StrategyPeriodMutationRow(
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
            new StrategyRecordRepository.StrategyPeriodMutationRow(
                strategyId,
                1,
                startSlot,
                Math.max(startSlot, firstEnd - 1),
                primaryAction(request.type()),
                0.85
            ),
            new StrategyRecordRepository.StrategyPeriodMutationRow(
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
        recordRepository.insertSnapshot(new StrategyRecordRepository.StrategySnapshotMutationRow(
            strategyId,
            simulationResult.estimatedRevenue(),
            simulationResult.confidenceLow(),
            simulationResult.confidenceHigh(),
            simulationResult.successProbability()
        ));
        revenueRepository.insertRevenueDaily(new StrategyRevenueRepository.RevenueDailyMutationRow(
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

    private Map<String, Object> mapListItem(StrategyRecordRepository.StrategyRow row) {
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

    private Map<String, Object> mapPeriod(StrategyRecordRepository.StrategyPeriodRow row) {
        return StrategySupport.orderedMap(
            "periodOrder", row.periodOrder(),
            "startSlot", row.startSlot(),
            "endSlot", row.endSlot(),
            "actionType", row.actionType(),
            "targetRatio", StrategySupport.round(row.targetRatio(), 3)
        );
    }

    private Map<String, Object> mapPricePeriod(StrategyPriceRepository.PricePeriodRow row) {
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

    private Map<String, Object> mapExecutionLog(StrategyRecordRepository.ExecutionLogRow row) {
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
