package cn.techstar.pvms.backend.module.strategy.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class StrategyRecordRepository {

    private final JdbcClient jdbcClient;

    public StrategyRecordRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<StrategyRow> findAll(LocalDate bizDate) {
        return jdbcClient.sql(baseSelect() + """
            ORDER BY sg.updated_at DESC, company.sort_index, station.sort_index
            """)
            .param("bizDate", bizDate)
            .query(this::mapStrategyRow)
            .list();
    }

    public StrategyRow findById(String id, LocalDate bizDate) {
        return jdbcClient.sql(baseSelect() + """
            WHERE sg.id = :id
            """)
            .param("id", id)
            .param("bizDate", bizDate)
            .query(this::mapStrategyRow)
            .list()
            .stream()
            .findFirst()
            .orElse(null);
    }

    public List<StrategyPeriodRow> findPeriodsByStrategyId(String strategyId) {
        return jdbcClient.sql("""
            SELECT strategy_id, period_order, start_slot, end_slot, action_type, target_ratio
            FROM sg_strategy_period
            WHERE strategy_id = :strategyId
            ORDER BY period_order
            """)
            .param("strategyId", strategyId)
            .query((resultSet, rowNum) -> new StrategyPeriodRow(
                resultSet.getString("strategy_id"),
                resultSet.getInt("period_order"),
                resultSet.getInt("start_slot"),
                resultSet.getInt("end_slot"),
                resultSet.getString("action_type"),
                resultSet.getDouble("target_ratio")
            ))
            .list();
    }

    public List<ExecutionLogRow> findExecutionLogsByStrategyId(String strategyId) {
        return jdbcClient.sql("""
            SELECT id, strategy_id, event_time, action, result, deviation_rate_pct, operator_name
            FROM sg_execution_log
            WHERE strategy_id = :strategyId
            ORDER BY event_time DESC
            """)
            .param("strategyId", strategyId)
            .query((resultSet, rowNum) -> new ExecutionLogRow(
                resultSet.getString("id"),
                resultSet.getString("strategy_id"),
                toLocalDateTime(resultSet.getTimestamp("event_time")),
                resultSet.getString("action"),
                resultSet.getString("result"),
                resultSet.getDouble("deviation_rate_pct"),
                resultSet.getString("operator_name")
            ))
            .list();
    }

    public List<ExecutionLogRow> findAllExecutionLogs() {
        return jdbcClient.sql("""
            SELECT id, strategy_id, event_time, action, result, deviation_rate_pct, operator_name
            FROM sg_execution_log
            ORDER BY event_time DESC
            """)
            .query((resultSet, rowNum) -> new ExecutionLogRow(
                resultSet.getString("id"),
                resultSet.getString("strategy_id"),
                toLocalDateTime(resultSet.getTimestamp("event_time")),
                resultSet.getString("action"),
                resultSet.getString("result"),
                resultSet.getDouble("deviation_rate_pct"),
                resultSet.getString("operator_name")
            ))
            .list();
    }

    public int insertStrategy(StrategyMutationRow row) {
        return jdbcClient.sql("""
            INSERT INTO sg_strategy (
                id, station_id, company_id, name, type, status, mode, target_power_kw,
                start_time, end_time, version_no, remark, created_at, updated_at
            ) VALUES (
                :id, :stationId, :companyId, :name, :type, :status, :mode, :targetPowerKw,
                :startTime, :endTime, :versionNo, :remark, :createdAt, :updatedAt
            )
            """)
            .param("id", row.id())
            .param("stationId", row.stationId())
            .param("companyId", row.companyId())
            .param("name", row.name())
            .param("type", row.type())
            .param("status", row.status())
            .param("mode", row.mode())
            .param("targetPowerKw", row.targetPowerKw())
            .param("startTime", row.startTime())
            .param("endTime", row.endTime())
            .param("versionNo", row.versionNo())
            .param("remark", row.remark())
            .param("createdAt", row.createdAt())
            .param("updatedAt", row.updatedAt())
            .update();
    }

    public int insertPeriod(StrategyPeriodMutationRow row) {
        return jdbcClient.sql("""
            INSERT INTO sg_strategy_period (
                strategy_id, period_order, start_slot, end_slot, action_type, target_ratio
            ) VALUES (
                :strategyId, :periodOrder, :startSlot, :endSlot, :actionType, :targetRatio
            )
            """)
            .param("strategyId", row.strategyId())
            .param("periodOrder", row.periodOrder())
            .param("startSlot", row.startSlot())
            .param("endSlot", row.endSlot())
            .param("actionType", row.actionType())
            .param("targetRatio", row.targetRatio())
            .update();
    }

    public int insertExecutionLog(ExecutionLogMutationRow row) {
        return jdbcClient.sql("""
            INSERT INTO sg_execution_log (
                id, strategy_id, event_time, action, result, deviation_rate_pct, operator_name
            ) VALUES (
                :id, :strategyId, :eventTime, :action, :result, :deviationRatePct, :operatorName
            )
            """)
            .param("id", row.id())
            .param("strategyId", row.strategyId())
            .param("eventTime", row.eventTime())
            .param("action", row.action())
            .param("result", row.result())
            .param("deviationRatePct", row.deviationRatePct())
            .param("operatorName", row.operatorName())
            .update();
    }

    public int insertSnapshot(StrategySnapshotMutationRow row) {
        return jdbcClient.sql("""
            INSERT INTO sg_strategy_snapshot (
                strategy_id, last_simulated_revenue_cny, confidence_low_cny,
                confidence_high_cny, success_probability_pct
            ) VALUES (
                :strategyId, :lastSimulatedRevenueCny, :confidenceLowCny,
                :confidenceHighCny, :successProbabilityPct
            )
            """)
            .param("strategyId", row.strategyId())
            .param("lastSimulatedRevenueCny", row.lastSimulatedRevenueCny())
            .param("confidenceLowCny", row.confidenceLowCny())
            .param("confidenceHighCny", row.confidenceHighCny())
            .param("successProbabilityPct", row.successProbabilityPct())
            .update();
    }

    public int deleteSnapshot(String strategyId) {
        return jdbcClient.sql("DELETE FROM sg_strategy_snapshot WHERE strategy_id = :strategyId")
            .param("strategyId", strategyId)
            .update();
    }

    public int updateSnapshot(StrategySnapshotMutationRow row) {
        return jdbcClient.sql("""
            UPDATE sg_strategy_snapshot
            SET last_simulated_revenue_cny = :lastSimulatedRevenueCny,
                confidence_low_cny = :confidenceLowCny,
                confidence_high_cny = :confidenceHighCny,
                success_probability_pct = :successProbabilityPct
            WHERE strategy_id = :strategyId
            """)
            .param("strategyId", row.strategyId())
            .param("lastSimulatedRevenueCny", row.lastSimulatedRevenueCny())
            .param("confidenceLowCny", row.confidenceLowCny())
            .param("confidenceHighCny", row.confidenceHighCny())
            .param("successProbabilityPct", row.successProbabilityPct())
            .update();
    }

    public int updateStatus(String id, String status, LocalDateTime updatedAt) {
        return jdbcClient.sql("""
            UPDATE sg_strategy
            SET status = :status,
                updated_at = :updatedAt
            WHERE id = :id
            """)
            .param("id", id)
            .param("status", status)
            .param("updatedAt", updatedAt)
            .update();
    }

    public int deleteRevenueDaily(String strategyId) {
        return jdbcClient.sql("DELETE FROM sg_revenue_daily WHERE strategy_id = :strategyId")
            .param("strategyId", strategyId)
            .update();
    }

    public int deleteExecutionLogs(String strategyId) {
        return jdbcClient.sql("DELETE FROM sg_execution_log WHERE strategy_id = :strategyId")
            .param("strategyId", strategyId)
            .update();
    }

    public int deletePeriods(String strategyId) {
        return jdbcClient.sql("DELETE FROM sg_strategy_period WHERE strategy_id = :strategyId")
            .param("strategyId", strategyId)
            .update();
    }

    public int deleteStrategy(String strategyId) {
        return jdbcClient.sql("DELETE FROM sg_strategy WHERE id = :strategyId")
            .param("strategyId", strategyId)
            .update();
    }

    private String baseSelect() {
        return """
            SELECT
                sg.id,
                sg.station_id,
                sg.company_id,
                sg.name,
                sg.type,
                sg.status,
                sg.mode,
                sg.target_power_kw,
                sg.start_time,
                sg.end_time,
                sg.version_no,
                sg.remark,
                sg.created_at,
                sg.updated_at,
                station.name AS station_name,
                station.capacity_kwp,
                station.status AS station_status,
                station.data_quality,
                station.sort_index AS station_sort_index,
                company.name AS company_name,
                company.region,
                company.sort_index AS company_sort_index,
                snapshot.last_simulated_revenue_cny,
                snapshot.confidence_low_cny,
                snapshot.confidence_high_cny,
                snapshot.success_probability_pct,
                revenue.estimated_revenue_cny AS today_estimated_revenue_cny,
                revenue.actual_revenue_cny AS today_actual_revenue_cny
            FROM sg_strategy sg
            JOIN sa_station station ON station.id = sg.station_id
            JOIN sa_company company ON company.id = sg.company_id
            LEFT JOIN sg_strategy_snapshot snapshot ON snapshot.strategy_id = sg.id
            LEFT JOIN sg_revenue_daily revenue
              ON revenue.strategy_id = sg.id
             AND revenue.biz_date = :bizDate
            """;
    }

    private StrategyRow mapStrategyRow(java.sql.ResultSet resultSet, int rowNum) throws java.sql.SQLException {
        return new StrategyRow(
            resultSet.getString("id"),
            resultSet.getString("station_id"),
            resultSet.getString("company_id"),
            resultSet.getString("name"),
            resultSet.getString("type"),
            resultSet.getString("status"),
            resultSet.getString("mode"),
            resultSet.getDouble("target_power_kw"),
            toLocalDateTime(resultSet.getTimestamp("start_time")),
            toLocalDateTime(resultSet.getTimestamp("end_time")),
            resultSet.getInt("version_no"),
            resultSet.getString("remark"),
            toLocalDateTime(resultSet.getTimestamp("created_at")),
            toLocalDateTime(resultSet.getTimestamp("updated_at")),
            resultSet.getString("station_name"),
            resultSet.getDouble("capacity_kwp"),
            resultSet.getString("station_status"),
            resultSet.getString("data_quality"),
            resultSet.getInt("station_sort_index"),
            resultSet.getString("company_name"),
            resultSet.getString("region"),
            resultSet.getInt("company_sort_index"),
            nullableDouble(resultSet, "last_simulated_revenue_cny"),
            nullableDouble(resultSet, "confidence_low_cny"),
            nullableDouble(resultSet, "confidence_high_cny"),
            nullableDouble(resultSet, "success_probability_pct"),
            nullableDouble(resultSet, "today_estimated_revenue_cny"),
            nullableDouble(resultSet, "today_actual_revenue_cny")
        );
    }

    private Double nullableDouble(java.sql.ResultSet resultSet, String column) throws java.sql.SQLException {
        double value = resultSet.getDouble(column);
        return resultSet.wasNull() ? null : value;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    public record StrategyRow(
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

    public record StrategyPeriodRow(
        String strategyId,
        int periodOrder,
        int startSlot,
        int endSlot,
        String actionType,
        double targetRatio
    ) {
    }

    public record ExecutionLogRow(
        String id,
        String strategyId,
        LocalDateTime eventTime,
        String action,
        String result,
        double deviationRatePct,
        String operatorName
    ) {
    }

    public record StrategyMutationRow(
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

    public record StrategyPeriodMutationRow(
        String strategyId,
        int periodOrder,
        int startSlot,
        int endSlot,
        String actionType,
        double targetRatio
    ) {
    }

    public record ExecutionLogMutationRow(
        String id,
        String strategyId,
        LocalDateTime eventTime,
        String action,
        String result,
        double deviationRatePct,
        String operatorName
    ) {
    }

    public record StrategySnapshotMutationRow(
        String strategyId,
        double lastSimulatedRevenueCny,
        double confidenceLowCny,
        double confidenceHighCny,
        double successProbabilityPct
    ) {
    }
}
