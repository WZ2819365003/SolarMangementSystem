package cn.techstar.pvms.backend.module.productionmonitor.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ProductionMonitorWeatherSnapshotRepository {

    private final JdbcClient jdbcClient;

    public ProductionMonitorWeatherSnapshotRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<WeatherSnapshotRow> findAll() {
        return jdbcClient.sql("""
            SELECT
                resource_unit_id,
                snapshot_time,
                weather,
                cloudiness,
                temperature,
                irradiance,
                humidity,
                wind_speed,
                conclusion
            FROM pm_weather_snapshot
            ORDER BY resource_unit_id
            """)
            .query((resultSet, rowNum) -> new WeatherSnapshotRow(
                resultSet.getString("resource_unit_id"),
                toLocalDateTime(resultSet.getTimestamp("snapshot_time")),
                resultSet.getString("weather"),
                resultSet.getString("cloudiness"),
                resultSet.getInt("temperature"),
                resultSet.getInt("irradiance"),
                resultSet.getInt("humidity"),
                resultSet.getDouble("wind_speed"),
                resultSet.getString("conclusion")
            ))
            .list();
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    public record WeatherSnapshotRow(
        String resourceUnitId,
        LocalDateTime snapshotTime,
        String weather,
        String cloudiness,
        int temperature,
        int irradiance,
        int humidity,
        double windSpeed,
        String conclusion
    ) {
    }
}
