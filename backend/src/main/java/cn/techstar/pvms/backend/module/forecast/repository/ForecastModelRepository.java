package cn.techstar.pvms.backend.module.forecast.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ForecastModelRepository {

    private final JdbcClient jdbcClient;

    public ForecastModelRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<ModelRow> findAll() {
        return jdbcClient.sql("""
            SELECT id, name, type, version, provider, description, status
            FROM fc_model
            ORDER BY type, id
            """)
            .query(this::mapRow)
            .list();
    }

    private ModelRow mapRow(java.sql.ResultSet resultSet, int rowNum) throws java.sql.SQLException {
        return new ModelRow(
            resultSet.getString("id"),
            resultSet.getString("name"),
            resultSet.getString("type"),
            resultSet.getString("version"),
            resultSet.getString("provider"),
            resultSet.getString("description"),
            resultSet.getString("status")
        );
    }

    public record ModelRow(
        String id,
        String name,
        String type,
        String version,
        String provider,
        String description,
        String status
    ) {
    }
}
