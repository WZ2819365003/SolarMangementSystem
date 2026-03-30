package cn.techstar.pvms.backend.module.forecast.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ForecastMonthlyAccuracyRepository {

    private final JdbcClient jdbcClient;

    public ForecastMonthlyAccuracyRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<MonthlyAccuracyRow> findAll() {
        return jdbcClient.sql("""
            SELECT
                m.station_id,
                s.name AS station_name,
                c.region,
                s.sort_index,
                m.month_key,
                m.forecast_type,
                m.mae_kw,
                m.rmse_kw,
                m.accuracy_pct
            FROM fc_monthly_accuracy_snapshot m
            JOIN sa_station s ON s.id = m.station_id
            JOIN sa_company c ON c.id = s.company_id
            ORDER BY m.month_key, s.sort_index, m.forecast_type
            """)
            .query(this::mapRow)
            .list();
    }

    private MonthlyAccuracyRow mapRow(java.sql.ResultSet resultSet, int rowNum) throws java.sql.SQLException {
        return new MonthlyAccuracyRow(
            resultSet.getString("station_id"),
            resultSet.getString("station_name"),
            resultSet.getString("region"),
            resultSet.getInt("sort_index"),
            resultSet.getString("month_key"),
            resultSet.getString("forecast_type"),
            resultSet.getDouble("mae_kw"),
            resultSet.getDouble("rmse_kw"),
            resultSet.getDouble("accuracy_pct")
        );
    }

    public record MonthlyAccuracyRow(
        String stationId,
        String stationName,
        String region,
        int sortIndex,
        String monthKey,
        String forecastType,
        double maeKw,
        double rmseKw,
        double accuracyPct
    ) {
    }
}
