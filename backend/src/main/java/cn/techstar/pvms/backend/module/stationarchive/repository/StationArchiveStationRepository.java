package cn.techstar.pvms.backend.module.stationarchive.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class StationArchiveStationRepository {

    private final JdbcClient jdbcClient;

    public StationArchiveStationRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<StationRow> findAll() {
        return jdbcClient.sql("""
            SELECT
                s.id,
                s.company_id,
                c.name AS company_name,
                s.name,
                s.capacity_kwp,
                s.status,
                s.data_quality,
                s.grid_status,
                s.grid_status_label,
                s.commission_date,
                s.address,
                s.load_base_kw,
                s.sort_index
            FROM sa_station s
            JOIN sa_company c ON c.id = s.company_id
            ORDER BY s.sort_index
            """)
            .query((resultSet, rowNum) -> new StationRow(
                resultSet.getString("id"),
                resultSet.getString("company_id"),
                resultSet.getString("company_name"),
                resultSet.getString("name"),
                resultSet.getDouble("capacity_kwp"),
                resultSet.getString("status"),
                resultSet.getString("data_quality"),
                resultSet.getString("grid_status"),
                resultSet.getString("grid_status_label"),
                resultSet.getObject("commission_date", LocalDate.class),
                resultSet.getString("address"),
                resultSet.getDouble("load_base_kw"),
                resultSet.getInt("sort_index")
            ))
            .list();
    }

    public record StationRow(
        String id,
        String companyId,
        String companyName,
        String name,
        double capacityKwp,
        String status,
        String dataQuality,
        String gridStatus,
        String gridStatusLabel,
        LocalDate commissionDate,
        String address,
        double loadBaseKw,
        int sortIndex
    ) {
    }
}
