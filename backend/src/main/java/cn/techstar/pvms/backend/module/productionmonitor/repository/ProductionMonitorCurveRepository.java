package cn.techstar.pvms.backend.module.productionmonitor.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class ProductionMonitorCurveRepository {

    private final JdbcClient jdbcClient;

    public ProductionMonitorCurveRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<CurveRow> findByResourceUnitIdAndDate(String resourceUnitId, LocalDate bizDate) {
        return jdbcClient.sql("""
            SELECT
                s.id AS station_id,
                s.resource_unit_id,
                s.name AS station_name,
                s.capacity_mw,
                s.weight,
                s.status,
                s.sort_index,
                s.load_visible,
                c.biz_date,
                c.time_slot,
                c.load_kw,
                c.pv_power_kw,
                c.forecast_power_kw,
                c.baseline_power_kw,
                c.irradiance,
                c.temperature
            FROM pm_station_curve_15m c
            JOIN pm_station s ON s.id = c.station_id
            WHERE s.resource_unit_id = :resourceUnitId
              AND c.biz_date = :bizDate
            ORDER BY s.sort_index, c.time_slot
            """)
            .param("resourceUnitId", resourceUnitId)
            .param("bizDate", bizDate)
            .query(this::mapRow)
            .list();
    }

    public List<CurveRow> findLoadVisibleByDate(LocalDate bizDate) {
        return jdbcClient.sql("""
            SELECT
                s.id AS station_id,
                s.resource_unit_id,
                s.name AS station_name,
                s.capacity_mw,
                s.weight,
                s.status,
                s.sort_index,
                s.load_visible,
                c.biz_date,
                c.time_slot,
                c.load_kw,
                c.pv_power_kw,
                c.forecast_power_kw,
                c.baseline_power_kw,
                c.irradiance,
                c.temperature
            FROM pm_station_curve_15m c
            JOIN pm_station s ON s.id = c.station_id
            WHERE s.load_visible = TRUE
              AND c.biz_date = :bizDate
            ORDER BY s.sort_index, c.time_slot
            """)
            .param("bizDate", bizDate)
            .query(this::mapRow)
            .list();
    }

    private CurveRow mapRow(java.sql.ResultSet resultSet, int rowNum) throws java.sql.SQLException {
        return new CurveRow(
            resultSet.getString("station_id"),
            resultSet.getString("resource_unit_id"),
            resultSet.getString("station_name"),
            resultSet.getDouble("capacity_mw"),
            resultSet.getDouble("weight"),
            resultSet.getString("status"),
            resultSet.getInt("sort_index"),
            resultSet.getBoolean("load_visible"),
            resultSet.getObject("biz_date", LocalDate.class),
            resultSet.getInt("time_slot"),
            resultSet.getDouble("load_kw"),
            resultSet.getDouble("pv_power_kw"),
            resultSet.getDouble("forecast_power_kw"),
            resultSet.getDouble("baseline_power_kw"),
            resultSet.getInt("irradiance"),
            resultSet.getDouble("temperature")
        );
    }

    public record CurveRow(
        String stationId,
        String resourceUnitId,
        String stationName,
        double capacityMw,
        double weight,
        String status,
        int sortIndex,
        boolean loadVisible,
        LocalDate bizDate,
        int timeSlot,
        double loadKw,
        double pvPowerKw,
        double forecastPowerKw,
        double baselinePowerKw,
        int irradiance,
        double temperature
    ) {
    }
}
