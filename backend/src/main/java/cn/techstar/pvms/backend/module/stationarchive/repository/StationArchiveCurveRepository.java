package cn.techstar.pvms.backend.module.stationarchive.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class StationArchiveCurveRepository {

    private final JdbcClient jdbcClient;

    public StationArchiveCurveRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<CurveRow> findByDate(LocalDate bizDate) {
        return jdbcClient.sql("""
            SELECT
                s.id AS station_id,
                s.company_id,
                s.name AS station_name,
                s.status,
                s.load_base_kw,
                s.capacity_kwp,
                s.sort_index,
                c.biz_date,
                c.time_slot,
                c.load_kw,
                c.pv_output_kw,
                c.forecast_day_ahead_kw,
                c.forecast_ultra_short_kw
            FROM sa_station_curve_15m c
            JOIN sa_station s ON s.id = c.station_id
            WHERE c.biz_date = :bizDate
            ORDER BY s.sort_index, c.time_slot
            """)
            .param("bizDate", bizDate)
            .query(this::mapRow)
            .list();
    }

    public List<CurveRow> findByStationIdAndDate(String stationId, LocalDate bizDate) {
        return jdbcClient.sql("""
            SELECT
                s.id AS station_id,
                s.company_id,
                s.name AS station_name,
                s.status,
                s.load_base_kw,
                s.capacity_kwp,
                s.sort_index,
                c.biz_date,
                c.time_slot,
                c.load_kw,
                c.pv_output_kw,
                c.forecast_day_ahead_kw,
                c.forecast_ultra_short_kw
            FROM sa_station_curve_15m c
            JOIN sa_station s ON s.id = c.station_id
            WHERE c.station_id = :stationId
              AND c.biz_date = :bizDate
            ORDER BY c.time_slot
            """)
            .param("stationId", stationId)
            .param("bizDate", bizDate)
            .query(this::mapRow)
            .list();
    }

    private CurveRow mapRow(java.sql.ResultSet resultSet, int rowNum) throws java.sql.SQLException {
        return new CurveRow(
            resultSet.getString("station_id"),
            resultSet.getString("company_id"),
            resultSet.getString("station_name"),
            resultSet.getString("status"),
            resultSet.getDouble("load_base_kw"),
            resultSet.getDouble("capacity_kwp"),
            resultSet.getInt("sort_index"),
            resultSet.getObject("biz_date", LocalDate.class),
            resultSet.getInt("time_slot"),
            resultSet.getDouble("load_kw"),
            resultSet.getDouble("pv_output_kw"),
            resultSet.getDouble("forecast_day_ahead_kw"),
            resultSet.getDouble("forecast_ultra_short_kw")
        );
    }

    public record CurveRow(
        String stationId,
        String companyId,
        String stationName,
        String status,
        double loadBaseKw,
        double capacityKwp,
        int sortIndex,
        LocalDate bizDate,
        int timeSlot,
        double loadKw,
        double pvOutputKw,
        double forecastDayAheadKw,
        double forecastUltraShortKw
    ) {
    }
}
