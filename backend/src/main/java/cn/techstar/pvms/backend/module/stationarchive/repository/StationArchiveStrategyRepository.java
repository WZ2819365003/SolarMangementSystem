package cn.techstar.pvms.backend.module.stationarchive.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class StationArchiveStrategyRepository {

    private final JdbcClient jdbcClient;

    public StationArchiveStrategyRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<StrategyRow> findAll() {
        return jdbcClient.sql("""
            SELECT
                ss.station_id,
                s.name AS station_name,
                ss.name,
                ss.type,
                ss.status,
                ss.start_time,
                ss.end_time,
                ss.target_power_kw,
                ss.estimated_revenue_cny
            FROM sa_station_strategy ss
            JOIN sa_station s ON s.id = ss.station_id
            ORDER BY s.sort_index
            """)
            .query(this::mapRow)
            .list();
    }

    public StrategyRow findByStationId(String stationId) {
        return jdbcClient.sql("""
            SELECT
                ss.station_id,
                s.name AS station_name,
                ss.name,
                ss.type,
                ss.status,
                ss.start_time,
                ss.end_time,
                ss.target_power_kw,
                ss.estimated_revenue_cny
            FROM sa_station_strategy ss
            JOIN sa_station s ON s.id = ss.station_id
            WHERE ss.station_id = :stationId
            """)
            .param("stationId", stationId)
            .query(this::mapRow)
            .single();
    }

    private StrategyRow mapRow(java.sql.ResultSet resultSet, int rowNum) throws java.sql.SQLException {
        return new StrategyRow(
            resultSet.getString("station_id"),
            resultSet.getString("station_name"),
            resultSet.getString("name"),
            resultSet.getString("type"),
            resultSet.getString("status"),
            toLocalDateTime(resultSet.getTimestamp("start_time")),
            toLocalDateTime(resultSet.getTimestamp("end_time")),
            resultSet.getDouble("target_power_kw"),
            resultSet.getDouble("estimated_revenue_cny")
        );
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    public record StrategyRow(
        String stationId,
        String stationName,
        String name,
        String type,
        String status,
        LocalDateTime startTime,
        LocalDateTime endTime,
        double targetPowerKw,
        double estimatedRevenueCny
    ) {
    }
}
