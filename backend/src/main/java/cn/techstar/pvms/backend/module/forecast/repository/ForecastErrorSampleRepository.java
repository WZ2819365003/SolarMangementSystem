package cn.techstar.pvms.backend.module.forecast.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class ForecastErrorSampleRepository {

    private final JdbcClient jdbcClient;

    public ForecastErrorSampleRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<ErrorSampleRow> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return jdbcClient.sql("""
            SELECT
                e.station_id,
                s.name AS station_name,
                c.region,
                s.sort_index,
                e.biz_date,
                e.hour_slot,
                e.forecast_type,
                e.error_kw,
                e.qualified
            FROM fc_error_sample e
            JOIN sa_station s ON s.id = e.station_id
            JOIN sa_company c ON c.id = s.company_id
            WHERE e.biz_date BETWEEN :startDate AND :endDate
            ORDER BY e.biz_date, s.sort_index, e.forecast_type, e.hour_slot
            """)
            .param("startDate", startDate)
            .param("endDate", endDate)
            .query(this::mapRow)
            .list();
    }

    private ErrorSampleRow mapRow(java.sql.ResultSet resultSet, int rowNum) throws java.sql.SQLException {
        return new ErrorSampleRow(
            resultSet.getString("station_id"),
            resultSet.getString("station_name"),
            resultSet.getString("region"),
            resultSet.getInt("sort_index"),
            resultSet.getObject("biz_date", LocalDate.class),
            resultSet.getInt("hour_slot"),
            resultSet.getString("forecast_type"),
            resultSet.getDouble("error_kw"),
            resultSet.getBoolean("qualified")
        );
    }

    public record ErrorSampleRow(
        String stationId,
        String stationName,
        String region,
        int sortIndex,
        LocalDate bizDate,
        int hourSlot,
        String forecastType,
        double errorKw,
        boolean qualified
    ) {
    }
}
