package cn.techstar.pvms.backend.module.productionmonitor.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ProductionMonitorStationSnapshotRepository {

    private final JdbcClient jdbcClient;

    public ProductionMonitorStationSnapshotRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<StationSnapshotRow> findAll() {
        return jdbcClient.sql("""
            SELECT
                s.id,
                s.resource_unit_id,
                s.name,
                s.capacity_mw,
                s.weight,
                s.status,
                s.online_rate,
                s.alarm_count,
                s.sort_index,
                s.load_visible,
                s.load_base_kw,
                rs.snapshot_time,
                rs.realtime_power_kw,
                rs.load_kw,
                rs.availability,
                rs.health_grade
            FROM pm_station s
            JOIN pm_station_realtime_snapshot rs ON rs.station_id = s.id
            ORDER BY s.sort_index
            """)
            .query((resultSet, rowNum) -> new StationSnapshotRow(
                resultSet.getString("id"),
                resultSet.getString("resource_unit_id"),
                resultSet.getString("name"),
                resultSet.getDouble("capacity_mw"),
                resultSet.getDouble("weight"),
                resultSet.getString("status"),
                resultSet.getDouble("online_rate"),
                resultSet.getInt("alarm_count"),
                resultSet.getInt("sort_index"),
                resultSet.getBoolean("load_visible"),
                resultSet.getDouble("load_base_kw"),
                toLocalDateTime(resultSet.getTimestamp("snapshot_time")),
                resultSet.getDouble("realtime_power_kw"),
                resultSet.getDouble("load_kw"),
                resultSet.getDouble("availability"),
                resultSet.getString("health_grade")
            ))
            .list();
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    public record StationSnapshotRow(
        String id,
        String resourceUnitId,
        String name,
        double capacityMw,
        double weight,
        String status,
        double onlineRate,
        int alarmCount,
        int sortIndex,
        boolean loadVisible,
        double loadBaseKw,
        LocalDateTime snapshotTime,
        double realtimePowerKw,
        double loadKw,
        double availability,
        String healthGrade
    ) {
    }
}
