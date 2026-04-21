package cn.techstar.pvms.backend.module.stationarchive.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StationArchiveCompanyRepository {

    private final JdbcClient jdbcClient;

    public StationArchiveCompanyRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<CompanyRow> findAll() {
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

    public record CompanyRow(
        String id,
        String name,
        String region,
        int sortIndex
    ) {
    }
}
