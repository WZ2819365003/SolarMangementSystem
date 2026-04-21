package cn.techstar.pvms.backend.module.dashboard.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class DashboardStationGeoRepository {

    private final JdbcClient jdbcClient;

    public DashboardStationGeoRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<StationGeoRow> findAll() {
        return jdbcClient.sql("""
            SELECT
                s.id,
                s.name,
                s.resource_unit_id,
                s.resource_unit_name,
                s.region,
                s.longitude,
                s.latitude,
                s.address,
                s.capacity_kwp,
                ss.status,
                ss.realtime_power_kw,
                ss.today_energy_kwh,
                ss.today_revenue_cny,
                ss.availability,
                ss.health_grade,
                ss.snapshot_time
            FROM dashboard_station s
            JOIN dashboard_station_status_snapshot ss ON ss.station_id = s.id
            ORDER BY s.id
            """)
            .query((resultSet, rowNum) -> new StationGeoRow(
                resultSet.getString("id"),
                resultSet.getString("name"),
                resultSet.getString("resource_unit_id"),
                resultSet.getString("resource_unit_name"),
                resultSet.getString("region"),
                resultSet.getDouble("longitude"),
                resultSet.getDouble("latitude"),
                resultSet.getString("address"),
                resultSet.getDouble("capacity_kwp"),
                resultSet.getString("status"),
                resultSet.getDouble("realtime_power_kw"),
                resultSet.getDouble("today_energy_kwh"),
                resultSet.getDouble("today_revenue_cny"),
                resultSet.getDouble("availability"),
                resultSet.getString("health_grade"),
                toLocalDateTime(resultSet.getTimestamp("snapshot_time"))
            ))
            .list();
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    public record StationGeoRow(
        String id,
        String name,
        String resourceUnitId,
        String resourceUnitName,
        String region,
        double longitude,
        double latitude,
        String address,
        double capacityKwp,
        String status,
        double realtimePowerKw,
        double todayEnergyKwh,
        double todayRevenueCny,
        double availability,
        String healthGrade,
        LocalDateTime snapshotTime
    ) {
    }
}
