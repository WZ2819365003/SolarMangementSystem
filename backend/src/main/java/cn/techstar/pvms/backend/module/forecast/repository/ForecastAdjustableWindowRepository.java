package cn.techstar.pvms.backend.module.forecast.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class ForecastAdjustableWindowRepository {

    private final JdbcClient jdbcClient;

    public ForecastAdjustableWindowRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<AdjustableWindowRow> findByDate(LocalDate bizDate) {
        return jdbcClient.sql("""
            SELECT
                w.station_id,
                s.name AS station_name,
                c.region,
                s.sort_index,
                w.biz_date,
                w.window_order,
                w.start_slot,
                w.end_slot,
                w.window_status
            FROM fc_adjustable_window w
            JOIN sa_station s ON s.id = w.station_id
            JOIN sa_company c ON c.id = s.company_id
            WHERE w.biz_date = :bizDate
            ORDER BY s.sort_index, w.window_order
            """)
            .param("bizDate", bizDate)
            .query(this::mapRow)
            .list();
    }

    private AdjustableWindowRow mapRow(java.sql.ResultSet resultSet, int rowNum) throws java.sql.SQLException {
        return new AdjustableWindowRow(
            resultSet.getString("station_id"),
            resultSet.getString("station_name"),
            resultSet.getString("region"),
            resultSet.getInt("sort_index"),
            resultSet.getObject("biz_date", LocalDate.class),
            resultSet.getInt("window_order"),
            resultSet.getInt("start_slot"),
            resultSet.getInt("end_slot"),
            resultSet.getString("window_status")
        );
    }

    public record AdjustableWindowRow(
        String stationId,
        String stationName,
        String region,
        int sortIndex,
        LocalDate bizDate,
        int windowOrder,
        int startSlot,
        int endSlot,
        String windowStatus
    ) {
    }
}
