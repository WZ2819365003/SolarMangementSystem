package cn.techstar.pvms.backend.module.productionmonitor.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ProductionMonitorDispatchRecordRepository {

    private final JdbcClient jdbcClient;

    public ProductionMonitorDispatchRecordRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<DispatchRecordRow> findByResourceUnitId(String resourceUnitId) {
        return jdbcClient.sql("""
            SELECT
                id,
                resource_unit_id,
                issued_at,
                command_type,
                target_power_mw,
                actual_power_mw,
                response_seconds,
                status,
                deviation_reason
            FROM pm_dispatch_record
            WHERE resource_unit_id = :resourceUnitId
            ORDER BY issued_at
            """)
            .param("resourceUnitId", resourceUnitId)
            .query((resultSet, rowNum) -> new DispatchRecordRow(
                resultSet.getString("id"),
                resultSet.getString("resource_unit_id"),
                toLocalDateTime(resultSet.getTimestamp("issued_at")),
                resultSet.getString("command_type"),
                resultSet.getDouble("target_power_mw"),
                resultSet.getDouble("actual_power_mw"),
                resultSet.getInt("response_seconds"),
                resultSet.getString("status"),
                resultSet.getString("deviation_reason")
            ))
            .list();
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    public record DispatchRecordRow(
        String id,
        String resourceUnitId,
        LocalDateTime issuedAt,
        String commandType,
        double targetPowerMw,
        double actualPowerMw,
        int responseSeconds,
        String status,
        String deviationReason
    ) {
    }
}
