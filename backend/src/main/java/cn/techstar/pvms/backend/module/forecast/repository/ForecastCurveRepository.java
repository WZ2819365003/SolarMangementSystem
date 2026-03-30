package cn.techstar.pvms.backend.module.forecast.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class ForecastCurveRepository {

    private final JdbcClient jdbcClient;

    public ForecastCurveRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<CurveRow> findByDate(LocalDate bizDate) {
        return jdbcClient.sql("""
            SELECT
                s.id AS station_id,
                s.name AS station_name,
                c.region,
                s.status,
                s.data_quality,
                s.sort_index,
                sc.biz_date,
                sc.time_slot,
                sc.load_kw,
                sc.pv_output_kw,
                sc.forecast_day_ahead_kw,
                sc.forecast_ultra_short_kw
            FROM sa_station_curve_15m sc
            JOIN sa_station s ON s.id = sc.station_id
            JOIN sa_company c ON c.id = s.company_id
            WHERE sc.biz_date = :bizDate
            ORDER BY s.sort_index, sc.time_slot
            """)
            .param("bizDate", bizDate)
            .query(this::mapRow)
            .list();
    }

    private CurveRow mapRow(java.sql.ResultSet resultSet, int rowNum) throws java.sql.SQLException {
        return new CurveRow(
            resultSet.getString("station_id"),
            resultSet.getString("station_name"),
            resultSet.getString("region"),
            resultSet.getString("status"),
            resultSet.getString("data_quality"),
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
        String stationName,
        String region,
        String status,
        String dataQuality,
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
