package cn.techstar.pvms.backend.module.strategy.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface StrategyRecordMapper {

    record StrategyRow(
        String id,
        String stationId,
        String companyId,
        String name,
        String type,
        String status,
        String mode,
        double targetPowerKw,
        LocalDateTime startTime,
        LocalDateTime endTime,
        int versionNo,
        String remark,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String stationName,
        double capacityKwp,
        String stationStatus,
        String dataQuality,
        int stationSortIndex,
        String companyName,
        String region,
        int companySortIndex,
        Double lastSimulatedRevenueCny,
        Double confidenceLowCny,
        Double confidenceHighCny,
        Double successProbabilityPct,
        Double todayEstimatedRevenueCny,
        Double todayActualRevenueCny
    ) {
    }

    record StrategyPeriodRow(
        String strategyId,
        int periodOrder,
        int startSlot,
        int endSlot,
        String actionType,
        double targetRatio
    ) {
    }

    record ExecutionLogRow(
        String id,
        String strategyId,
        LocalDateTime eventTime,
        String action,
        String result,
        double deviationRatePct,
        String operatorName
    ) {
    }

    record StrategyMutationRow(
        String id,
        String stationId,
        String companyId,
        String name,
        String type,
        String status,
        String mode,
        double targetPowerKw,
        LocalDateTime startTime,
        LocalDateTime endTime,
        int versionNo,
        String remark,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
    }

    record StrategyPeriodMutationRow(
        String strategyId,
        int periodOrder,
        int startSlot,
        int endSlot,
        String actionType,
        double targetRatio
    ) {
    }

    record ExecutionLogMutationRow(
        String id,
        String strategyId,
        LocalDateTime eventTime,
        String action,
        String result,
        double deviationRatePct,
        String operatorName
    ) {
    }

    record StrategySnapshotMutationRow(
        String strategyId,
        double lastSimulatedRevenueCny,
        double confidenceLowCny,
        double confidenceHighCny,
        double successProbabilityPct
    ) {
    }

    List<StrategyRow> findAll(@Param("bizDate") LocalDate bizDate);

    StrategyRow findById(@Param("id") String id, @Param("bizDate") LocalDate bizDate);

    List<StrategyPeriodRow> findPeriodsByStrategyId(@Param("strategyId") String strategyId);

    List<ExecutionLogRow> findExecutionLogsByStrategyId(@Param("strategyId") String strategyId);

    List<ExecutionLogRow> findAllExecutionLogs();

    int insertStrategy(@Param("row") StrategyMutationRow row);

    int insertPeriod(@Param("row") StrategyPeriodMutationRow row);

    int insertExecutionLog(@Param("row") ExecutionLogMutationRow row);

    int insertSnapshot(@Param("row") StrategySnapshotMutationRow row);

    int deleteSnapshot(@Param("strategyId") String strategyId);

    int updateSnapshot(@Param("row") StrategySnapshotMutationRow row);

    int updateStatus(@Param("id") String id, @Param("status") String status, @Param("updatedAt") LocalDateTime updatedAt);

    int deleteRevenueDaily(@Param("strategyId") String strategyId);

    int deleteExecutionLogs(@Param("strategyId") String strategyId);

    int deletePeriods(@Param("strategyId") String strategyId);

    int deleteStrategy(@Param("strategyId") String strategyId);
}
