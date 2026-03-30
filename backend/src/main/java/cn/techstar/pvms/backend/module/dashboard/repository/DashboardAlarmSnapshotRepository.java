package cn.techstar.pvms.backend.module.dashboard.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class DashboardAlarmSnapshotRepository {

    private final JdbcClient jdbcClient;

    public DashboardAlarmSnapshotRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<AlarmSnapshotRow> findRecent(String level, String stationId) {
        return jdbcClient.sql("""
            SELECT
                a.id,
                a.station_id,
                s.name AS station_name,
                a.event_time,
                a.level,
                a.level_label,
                a.device_name,
                a.alarm_type,
                a.description,
                a.status,
                a.owner,
                a.suggestion
            FROM dashboard_alarm_snapshot a
            JOIN dashboard_station s ON s.id = a.station_id
            WHERE (:level IS NULL OR :level = '' OR a.level = :level)
              AND (:stationId IS NULL OR :stationId = '' OR a.station_id = :stationId)
            ORDER BY a.event_time DESC, a.id DESC
            """)
            .param("level", level)
            .param("stationId", stationId)
            .query((resultSet, rowNum) -> new AlarmSnapshotRow(
                resultSet.getString("id"),
                resultSet.getString("station_id"),
                resultSet.getString("station_name"),
                toLocalDateTime(resultSet.getTimestamp("event_time")),
                resultSet.getString("level"),
                resultSet.getString("level_label"),
                resultSet.getString("device_name"),
                resultSet.getString("alarm_type"),
                resultSet.getString("description"),
                resultSet.getString("status"),
                resultSet.getString("owner"),
                resultSet.getString("suggestion")
            ))
            .list();
    }

    public AlarmSummary summary() {
        return jdbcClient.sql("""
            SELECT
                SUM(CASE WHEN level = 'critical' THEN 1 ELSE 0 END) AS critical_count,
                SUM(CASE WHEN level = 'major' THEN 1 ELSE 0 END) AS major_count,
                SUM(CASE WHEN level = 'minor' THEN 1 ELSE 0 END) AS minor_count,
                SUM(CASE WHEN level = 'hint' THEN 1 ELSE 0 END) AS hint_count
            FROM dashboard_alarm_snapshot
            """)
            .query((resultSet, rowNum) -> new AlarmSummary(
                resultSet.getLong("critical_count"),
                resultSet.getLong("major_count"),
                resultSet.getLong("minor_count"),
                resultSet.getLong("hint_count")
            ))
            .single();
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    public record AlarmSnapshotRow(
        String id,
        String stationId,
        String stationName,
        LocalDateTime eventTime,
        String level,
        String levelLabel,
        String deviceName,
        String alarmType,
        String description,
        String status,
        String owner,
        String suggestion
    ) {
    }

    public record AlarmSummary(
        long critical,
        long major,
        long minor,
        long hint
    ) {
    }
}
