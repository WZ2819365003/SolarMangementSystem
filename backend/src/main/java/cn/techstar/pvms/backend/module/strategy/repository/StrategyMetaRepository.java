package cn.techstar.pvms.backend.module.strategy.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StrategyMetaRepository {

    private final JdbcClient jdbcClient;

    public StrategyMetaRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<CompanyRow> findCompanies() {
        return jdbcClient.sql("""
            SELECT id, name, region, sort_index
            FROM sa_company
            ORDER BY sort_index
            """)
            .query((resultSet, rowNum) -> new CompanyRow(
                resultSet.getString("id"),
                resultSet.getString("name"),
                resultSet.getString("region"),
                resultSet.getInt("sort_index")
            ))
            .list();
    }

    public List<StationRow> findStations() {
        return jdbcClient.sql("""
            SELECT
                s.id,
                s.company_id,
                c.name AS company_name,
                c.region,
                s.name,
                s.capacity_kwp,
                s.status,
                s.data_quality,
                s.sort_index
            FROM sa_station s
            JOIN sa_company c ON c.id = s.company_id
            ORDER BY c.sort_index, s.sort_index
            """)
            .query((resultSet, rowNum) -> new StationRow(
                resultSet.getString("id"),
                resultSet.getString("company_id"),
                resultSet.getString("company_name"),
                resultSet.getString("region"),
                resultSet.getString("name"),
                resultSet.getDouble("capacity_kwp"),
                resultSet.getString("status"),
                resultSet.getString("data_quality"),
                resultSet.getInt("sort_index")
            ))
            .list();
    }

    public record CompanyRow(
        String id,
        String name,
        String region,
        int sortIndex
    ) {
    }

    public record StationRow(
        String id,
        String companyId,
        String companyName,
        String region,
        String name,
        double capacityKwp,
        String status,
        String dataQuality,
        int sortIndex
    ) {
    }
}
