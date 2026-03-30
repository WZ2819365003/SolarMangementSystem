package cn.techstar.pvms.backend.module.forecast.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ForecastStationRepository {

    private final JdbcClient jdbcClient;

    public ForecastStationRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<StationRow> findAll() {
        return jdbcClient.sql("""
            SELECT
                s.id,
                s.name,
                s.company_id,
                c.name AS company_name,
                c.region,
                s.capacity_kwp,
                s.status,
                s.data_quality,
                s.sort_index
            FROM sa_station s
            JOIN sa_company c ON c.id = s.company_id
            ORDER BY c.sort_index, s.sort_index
            """)
            .query(this::mapRow)
            .list();
    }

    private StationRow mapRow(java.sql.ResultSet resultSet, int rowNum) throws java.sql.SQLException {
        return new StationRow(
            resultSet.getString("id"),
            resultSet.getString("name"),
            resultSet.getString("company_id"),
            resultSet.getString("company_name"),
            resultSet.getString("region"),
            resultSet.getDouble("capacity_kwp"),
            resultSet.getString("status"),
            resultSet.getString("data_quality"),
            resultSet.getInt("sort_index")
        );
    }

    public record StationRow(
        String id,
        String name,
        String companyId,
        String companyName,
        String region,
        double capacityKwp,
        String status,
        String dataQuality,
        int sortIndex
    ) {
    }
}
