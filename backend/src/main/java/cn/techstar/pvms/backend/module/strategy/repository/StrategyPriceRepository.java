package cn.techstar.pvms.backend.module.strategy.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StrategyPriceRepository {

    private final JdbcClient jdbcClient;

    public StrategyPriceRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<PricePeriodRow> findByStationId(String stationId) {
        return jdbcClient.sql("""
            SELECT
                station_id,
                period_order,
                start_slot,
                end_slot,
                price_type,
                price_cny_per_kwh
            FROM sg_price_period
            WHERE station_id = :stationId
            ORDER BY period_order
            """)
            .param("stationId", stationId)
            .query(this::mapRow)
            .list();
    }

    public List<PricePeriodRow> findTemplate() {
        return jdbcClient.sql("""
            SELECT
                station_id,
                period_order,
                start_slot,
                end_slot,
                price_type,
                price_cny_per_kwh
            FROM sg_price_period
            WHERE station_id = 'SZ-001'
            ORDER BY period_order
            """)
            .query(this::mapRow)
            .list();
    }

    private PricePeriodRow mapRow(java.sql.ResultSet resultSet, int rowNum) throws java.sql.SQLException {
        return new PricePeriodRow(
            resultSet.getString("station_id"),
            resultSet.getInt("period_order"),
            resultSet.getInt("start_slot"),
            resultSet.getInt("end_slot"),
            resultSet.getString("price_type"),
            resultSet.getDouble("price_cny_per_kwh")
        );
    }

    public record PricePeriodRow(
        String stationId,
        int periodOrder,
        int startSlot,
        int endSlot,
        String priceType,
        double priceCnyPerKwh
    ) {
    }
}
