package cn.techstar.pvms.backend.module.strategy.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StrategyTreeRepository {

    private final JdbcClient jdbcClient;

    public StrategyTreeRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<StationTreeRow> findCompanyStationRows() {
        return jdbcClient.sql("""
            SELECT
                c.id AS company_id,
                c.name AS company_name,
                c.region,
                c.sort_index AS company_sort_index,
                s.id AS station_id,
                s.name AS station_name,
                s.capacity_kwp,
                s.status AS station_status,
                s.data_quality,
                s.sort_index AS station_sort_index,
                COUNT(st.id) AS strategy_count,
                SUM(CASE WHEN st.status = 'executing' THEN 1 ELSE 0 END) AS active_count,
                SUM(CASE WHEN st.status = 'pending' THEN 1 ELSE 0 END) AS pending_count
            FROM sa_company c
            JOIN sa_station s ON s.company_id = c.id
            LEFT JOIN sg_strategy st ON st.station_id = s.id
            GROUP BY
                c.id, c.name, c.region, c.sort_index,
                s.id, s.name, s.capacity_kwp, s.status, s.data_quality, s.sort_index
            ORDER BY c.sort_index, s.sort_index
            """)
            .query((resultSet, rowNum) -> new StationTreeRow(
                resultSet.getString("company_id"),
                resultSet.getString("company_name"),
                resultSet.getString("region"),
                resultSet.getInt("company_sort_index"),
                resultSet.getString("station_id"),
                resultSet.getString("station_name"),
                resultSet.getDouble("capacity_kwp"),
                resultSet.getString("station_status"),
                resultSet.getString("data_quality"),
                resultSet.getInt("station_sort_index"),
                resultSet.getInt("strategy_count"),
                resultSet.getInt("active_count"),
                resultSet.getInt("pending_count")
            ))
            .list();
    }

    public record StationTreeRow(
        String companyId,
        String companyName,
        String region,
        int companySortIndex,
        String stationId,
        String stationName,
        double capacityKwp,
        String stationStatus,
        String dataQuality,
        int stationSortIndex,
        int strategyCount,
        int activeCount,
        int pendingCount
    ) {
    }
}
