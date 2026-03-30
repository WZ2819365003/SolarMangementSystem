package cn.techstar.pvms.backend.module.forecast.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class ForecastPredictionRepository {

    private final JdbcClient jdbcClient;

    public ForecastPredictionRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<PredictionRow> findByDate(LocalDate bizDate) {
        return jdbcClient.sql("""
            SELECT
                p.station_id,
                s.name AS station_name,
                c.region,
                s.sort_index,
                p.biz_date,
                p.time_slot,
                p.forecast_type,
                p.predicted_power_kw,
                p.upper_bound_kw,
                p.lower_bound_kw,
                p.scenario_tag
            FROM fc_prediction_series_15m p
            JOIN sa_station s ON s.id = p.station_id
            JOIN sa_company c ON c.id = s.company_id
            WHERE p.biz_date = :bizDate
            ORDER BY s.sort_index, p.forecast_type, p.time_slot
            """)
            .param("bizDate", bizDate)
            .query(this::mapRow)
            .list();
    }

    private PredictionRow mapRow(java.sql.ResultSet resultSet, int rowNum) throws java.sql.SQLException {
        return new PredictionRow(
            resultSet.getString("station_id"),
            resultSet.getString("station_name"),
            resultSet.getString("region"),
            resultSet.getInt("sort_index"),
            resultSet.getObject("biz_date", LocalDate.class),
            resultSet.getInt("time_slot"),
            resultSet.getString("forecast_type"),
            resultSet.getDouble("predicted_power_kw"),
            resultSet.getDouble("upper_bound_kw"),
            resultSet.getDouble("lower_bound_kw"),
            resultSet.getString("scenario_tag")
        );
    }

    public record PredictionRow(
        String stationId,
        String stationName,
        String region,
        int sortIndex,
        LocalDate bizDate,
        int timeSlot,
        String forecastType,
        double predictedPowerKw,
        double upperBoundKw,
        double lowerBoundKw,
        String scenarioTag
    ) {
    }
}
