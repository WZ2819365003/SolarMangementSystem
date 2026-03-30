package cn.techstar.pvms.backend.module.stationarchive.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class StationArchiveInverterAlarmRepository {

    private final JdbcClient jdbcClient;

    public StationArchiveInverterAlarmRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<InverterAlarmRow> findByInverterId(String inverterId) {
        return jdbcClient.sql("""
            SELECT id, inverter_id, event_time, type, level, description, status
            FROM sa_inverter_alarm
            WHERE inverter_id = :inverterId
            ORDER BY event_time DESC
            """)
            .param("inverterId", inverterId)
            .query((resultSet, rowNum) -> new InverterAlarmRow(
                resultSet.getString("id"),
                resultSet.getString("inverter_id"),
                toLocalDateTime(resultSet.getTimestamp("event_time")),
                resultSet.getString("type"),
                resultSet.getString("level"),
                resultSet.getString("description"),
                resultSet.getString("status")
            ))
            .list();
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    public record InverterAlarmRow(
        String id,
        String inverterId,
        LocalDateTime eventTime,
        String type,
        String level,
        String description,
        String status
    ) {
    }
}
