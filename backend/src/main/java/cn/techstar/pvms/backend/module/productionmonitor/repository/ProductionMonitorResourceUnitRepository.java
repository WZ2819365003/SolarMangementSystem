package cn.techstar.pvms.backend.module.productionmonitor.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ProductionMonitorResourceUnitRepository {

    private final JdbcClient jdbcClient;

    public ProductionMonitorResourceUnitRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<ResourceUnitRow> findAll() {
        return jdbcClient.sql("""
            SELECT
                id,
                name,
                region,
                city,
                status,
                cluster_radius_km,
                dispatch_mode,
                strategy_label,
                latest_alarm_title,
                latest_alarm_time,
                alarm_total,
                alarm_critical,
                alarm_major,
                alarm_minor
            FROM pm_resource_unit
            ORDER BY id
            """)
            .query((resultSet, rowNum) -> new ResourceUnitRow(
                resultSet.getString("id"),
                resultSet.getString("name"),
                resultSet.getString("region"),
                resultSet.getString("city"),
                resultSet.getString("status"),
                resultSet.getInt("cluster_radius_km"),
                resultSet.getString("dispatch_mode"),
                resultSet.getString("strategy_label"),
                resultSet.getString("latest_alarm_title"),
                toLocalDateTime(resultSet.getTimestamp("latest_alarm_time")),
                resultSet.getInt("alarm_total"),
                resultSet.getInt("alarm_critical"),
                resultSet.getInt("alarm_major"),
                resultSet.getInt("alarm_minor")
            ))
            .list();
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    public record ResourceUnitRow(
        String id,
        String name,
        String region,
        String city,
        String status,
        int clusterRadiusKm,
        String dispatchMode,
        String strategyLabel,
        String latestAlarmTitle,
        LocalDateTime latestAlarmTime,
        int alarmTotal,
        int alarmCritical,
        int alarmMajor,
        int alarmMinor
    ) {
    }
}
