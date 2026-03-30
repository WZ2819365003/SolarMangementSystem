package cn.techstar.pvms.backend.module.strategy.service;

import cn.techstar.pvms.backend.module.strategy.repository.StrategyRevenueRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StrategyRevenueService {

    private final StrategyRevenueRepository revenueRepository;

    public StrategyRevenueService(StrategyRevenueRepository revenueRepository) {
        this.revenueRepository = revenueRepository;
    }

    public Map<String, Object> getRevenueSummary(String region, String type, String stationId) {
        List<StrategyRevenueRepository.RevenueDailyRow> filteredRows = filterRevenueRows(
            revenueRepository.findByDateRange(StrategySupport.DEFAULT_BIZ_DATE.minusDays(13), StrategySupport.DEFAULT_BIZ_DATE),
            region,
            type,
            stationId
        );

        Map<LocalDate, List<StrategyRevenueRepository.RevenueDailyRow>> rowsByDate = filteredRows.stream()
            .collect(Collectors.groupingBy(
                StrategyRevenueRepository.RevenueDailyRow::bizDate,
                LinkedHashMap::new,
                Collectors.toList()
            ));
        List<LocalDate> orderedDates = new ArrayList<>(new LinkedHashSet<>(rowsByDate.keySet()));
        orderedDates.sort(LocalDate::compareTo);

        List<String> dates = orderedDates.stream().map(LocalDate::toString).toList();
        List<Double> estimated = orderedDates.stream()
            .map(date -> StrategySupport.round(rowsByDate.getOrDefault(date, List.of()).stream().mapToDouble(StrategyRevenueRepository.RevenueDailyRow::estimatedRevenueCny).sum(), 2))
            .toList();
        List<Double> actual = orderedDates.stream()
            .map(date -> StrategySupport.round(rowsByDate.getOrDefault(date, List.of()).stream().mapToDouble(StrategyRevenueRepository.RevenueDailyRow::actualRevenueCny).sum(), 2))
            .toList();

        List<StrategyRevenueRepository.RevenueDailyRow> todayRows = filteredRows.stream()
            .filter(item -> Objects.equals(item.bizDate(), StrategySupport.DEFAULT_BIZ_DATE))
            .toList();

        List<Map<String, Object>> typeBreakdown = StrategySupport.TYPE_ORDER.stream()
            .map(value -> StrategySupport.orderedMap(
                "type", value,
                "label", StrategySupport.typeLabel(value),
                "actualRevenue", StrategySupport.round(todayRows.stream()
                    .filter(item -> Objects.equals(item.type(), value))
                    .mapToDouble(StrategyRevenueRepository.RevenueDailyRow::actualRevenueCny)
                    .sum(), 2),
                "estimatedRevenue", StrategySupport.round(todayRows.stream()
                    .filter(item -> Objects.equals(item.type(), value))
                    .mapToDouble(StrategyRevenueRepository.RevenueDailyRow::estimatedRevenueCny)
                    .sum(), 2)
            ))
            .toList();

        double todayRevenue = todayRows.stream().mapToDouble(StrategyRevenueRepository.RevenueDailyRow::actualRevenueCny).sum();
        double todayEstimated = todayRows.stream().mapToDouble(StrategyRevenueRepository.RevenueDailyRow::estimatedRevenueCny).sum();

        return StrategySupport.orderedMap(
            "kpi", StrategySupport.orderedMap(
                "todayRevenue", StrategySupport.round(todayRevenue, 2),
                "todayEstimatedRevenue", StrategySupport.round(todayEstimated, 2),
                "revenueAchievementRate", todayEstimated == 0 ? 0 : StrategySupport.round(todayRevenue * 100.0 / todayEstimated, 1),
                "strategyCount", todayRows.stream().map(StrategyRevenueRepository.RevenueDailyRow::strategyId).distinct().count()
            ),
            "trend", StrategySupport.orderedMap(
                "dates", dates,
                "estimatedRevenue", estimated,
                "actualRevenue", actual
            ),
            "typeBreakdown", typeBreakdown
        );
    }

    public Map<String, Object> getRevenueDetail(String region, String type, String stationId, LocalDate startDate, LocalDate endDate) {
        LocalDate resolvedEndDate = endDate == null ? StrategySupport.DEFAULT_BIZ_DATE : endDate;
        LocalDate resolvedStartDate = startDate == null ? resolvedEndDate.minusDays(13) : startDate;
        List<StrategyRevenueRepository.RevenueDailyRow> rows = filterRevenueRows(
            revenueRepository.findByDateRange(resolvedStartDate, resolvedEndDate),
            region,
            type,
            stationId
        ).stream()
            .sorted(Comparator.comparing(StrategyRevenueRepository.RevenueDailyRow::bizDate).reversed()
                .thenComparing(StrategyRevenueRepository.RevenueDailyRow::strategyId))
            .toList();

        return StrategySupport.orderedMap(
            "startDate", resolvedStartDate.toString(),
            "endDate", resolvedEndDate.toString(),
            "items", rows.stream().map(this::mapRevenueItem).toList(),
            "total", rows.size()
        );
    }

    public Map<String, Object> compare(List<String> ids) {
        Set<String> selectedIds = ids == null || ids.isEmpty()
            ? revenueRepository.findAll().stream().map(StrategyRevenueRepository.RevenueDailyRow::strategyId).limit(2).collect(Collectors.toSet())
            : ids.stream().filter(item -> item != null && !item.isBlank()).collect(Collectors.toCollection(LinkedHashSet::new));

        List<StrategyRevenueRepository.RevenueDailyRow> selectedRows = revenueRepository.findAll().stream()
            .filter(item -> selectedIds.contains(item.strategyId()))
            .toList();

        Map<String, List<StrategyRevenueRepository.RevenueDailyRow>> rowsByStrategy = selectedRows.stream()
            .collect(Collectors.groupingBy(
                StrategyRevenueRepository.RevenueDailyRow::strategyId,
                LinkedHashMap::new,
                Collectors.toList()
            ));

        List<Map<String, Object>> items = rowsByStrategy.entrySet().stream()
            .map(entry -> {
                List<StrategyRevenueRepository.RevenueDailyRow> rows = entry.getValue();
                StrategyRevenueRepository.RevenueDailyRow latest = rows.stream()
                    .max(Comparator.comparing(StrategyRevenueRepository.RevenueDailyRow::bizDate))
                    .orElseThrow();
                double totalActual = rows.stream().mapToDouble(StrategyRevenueRepository.RevenueDailyRow::actualRevenueCny).sum();
                double totalEstimated = rows.stream().mapToDouble(StrategyRevenueRepository.RevenueDailyRow::estimatedRevenueCny).sum();
                return StrategySupport.orderedMap(
                    "id", latest.strategyId(),
                    "name", latest.strategyName(),
                    "type", latest.type(),
                    "typeLabel", StrategySupport.typeLabel(latest.type()),
                    "stationId", latest.stationId(),
                    "stationName", latest.stationName(),
                    "totalRevenue", StrategySupport.round(totalActual, 2),
                    "estimatedRevenue", StrategySupport.round(totalEstimated, 2),
                    "achievementRate", totalEstimated == 0 ? 0 : StrategySupport.round(totalActual * 100.0 / totalEstimated, 1)
                );
            })
            .toList();

        double totalDelta = items.size() < 2
            ? 0
            : ((Number) items.getFirst().get("totalRevenue")).doubleValue() - ((Number) items.get(1).get("totalRevenue")).doubleValue();

        return StrategySupport.orderedMap(
            "items", items,
            "totalDelta", StrategySupport.round(totalDelta, 2)
        );
    }

    private List<StrategyRevenueRepository.RevenueDailyRow> filterRevenueRows(
        List<StrategyRevenueRepository.RevenueDailyRow> rows,
        String region,
        String type,
        String stationId
    ) {
        return rows.stream()
            .filter(item -> region == null || region.isBlank() || Objects.equals(item.region(), region))
            .filter(item -> type == null || type.isBlank() || Objects.equals(item.type(), type))
            .filter(item -> stationId == null || stationId.isBlank() || Objects.equals(item.stationId(), stationId))
            .toList();
    }

    private Map<String, Object> mapRevenueItem(StrategyRevenueRepository.RevenueDailyRow row) {
        return StrategySupport.orderedMap(
            "strategyId", row.strategyId(),
            "strategyName", row.strategyName(),
            "type", row.type(),
            "typeLabel", StrategySupport.typeLabel(row.type()),
            "status", row.status(),
            "stationId", row.stationId(),
            "stationName", row.stationName(),
            "companyId", row.companyId(),
            "companyName", row.companyName(),
            "region", row.region(),
            "date", row.bizDate().toString(),
            "estimatedRevenue", StrategySupport.round(row.estimatedRevenueCny(), 2),
            "actualRevenue", StrategySupport.round(row.actualRevenueCny(), 2),
            "peakSaving", StrategySupport.round(row.peakSavingCny(), 2),
            "responseReward", StrategySupport.round(row.responseRewardCny(), 2),
            "penalty", StrategySupport.round(row.penaltyCny(), 2)
        );
    }
}
